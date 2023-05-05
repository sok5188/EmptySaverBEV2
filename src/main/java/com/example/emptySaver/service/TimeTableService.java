package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.TimeTableDto;
import com.example.emptySaver.domain.entity.*;
import com.example.emptySaver.repository.MemberRepository;
import com.example.emptySaver.repository.ScheduleRepository;
import com.example.emptySaver.repository.TimeTableRepository;
import com.example.emptySaver.utils.TimeDataToBitConverter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeTableService {
    @PersistenceContext
    private final EntityManager em;

    private final TimeDataToBitConverter bitConverter;
    private final TimeTableRepository timeTableRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    private Map<String,Integer> dayToIntMap = Map.of("월",0, "화", 1,"수",2,"목",3,"금",4,"토",5,"일",6);

    public TimeTableDto.TimeTableInfo getMemberTimeTableByDayNum(Long memberId,LocalDate startDate, LocalDate endDate){
        Member member = memberRepository.findById(memberId).get();

        Time_Table timeTable = member.getTimeTable();
        timeTable.calcAllWeekScheduleData();
        long[] weekScheduleData = timeTable.getWeekScheduleData();
        final List<Schedule> scheduleList = timeTable.getScheduleList();

        return calcTimeTableDataPerWeek(startDate,endDate,weekScheduleData,scheduleList);
    }

    private List<List<Boolean>> convertLongListToBitListsPerDay(List<Long> bitDataPerDays){
        List<List<Boolean>> bitListsPerDay = new ArrayList<>();

        for (Long bits: bitDataPerDays) {
            List<Boolean> bitList = new ArrayList<>();
            long moveBit = 1l;

            for (int i = 0; i < 48 ; i++) {
                long andOpResult = bits & moveBit;
                boolean result = false;

                if(andOpResult >0l)
                    result = true;

                bitList.add(result);
                moveBit <<= 1;
            }

            bitListsPerDay.add(bitList);
        }

        return bitListsPerDay;
    }

    private TimeTableDto.TimeTableInfo calcTimeTableDataPerWeek( final LocalDate startDate, final LocalDate endDate ,final long[] weekScheduleData, final List<Schedule> scheduleList){
        final int dayNum = (int) Duration.between(startDate.atStartOfDay(),endDate.atStartOfDay()).toDays() +1;
        final int startDayOfWeek = startDate.getDayOfWeek().getValue() -1; //월요일부터 0~6까지 정수

        List<Long> bitDataPerDays = new ArrayList<>();
        List<List<TimeTableDto.ScheduleDto>> scheduleListPerDays  = new ArrayList<List<TimeTableDto.ScheduleDto>>();

        int dayOfWeekIdx = startDayOfWeek;
        final int WEEK_MOD = 7;
        for (int i = 0; i < dayNum; i++) {  //주기 데이터 저장
            bitDataPerDays.add(weekScheduleData[dayOfWeekIdx]);
            ++dayOfWeekIdx;
            dayOfWeekIdx %= WEEK_MOD;
            scheduleListPerDays.add(new ArrayList<>());
        }

        List<Periodic_Schedule> periodicScheduleList = new ArrayList<>();
        List<Non_Periodic_Schedule> nonPeriodicScheduleList = new ArrayList<>();
        for (Schedule schedule: scheduleList) { //타입 분리
            if(schedule instanceof Periodic_Schedule){
                periodicScheduleList.add((Periodic_Schedule)schedule);
            }
            else {
                Non_Periodic_Schedule nonPeriodicSchedule = (Non_Periodic_Schedule) schedule;
                if((nonPeriodicSchedule.getStartTime().isAfter(startDate.atStartOfDay())
                        && nonPeriodicSchedule.getEndTime().isBefore(endDate.atStartOfDay()) ))  //날짜 범위 내의 데이터만 저장
                    nonPeriodicScheduleList.add((Non_Periodic_Schedule)schedule);
            }

        }

        List<List<TimeTableDto.ScheduleDto>> weekRoutines = new ArrayList<>();
        for (int i = 0; i <WEEK_MOD ; i++)    //init
            weekRoutines.add(new ArrayList<>());

        for (Periodic_Schedule schedule:periodicScheduleList) { //weekRoutine인 스케줄 저장
            long[] weekBits = schedule.getWeekScheduleData();
            for(int day =0; day< WEEK_MOD ; ++day)
                if(weekBits[day] >0) {  //Dto Convert
                    weekRoutines.get(day).add(
                            TimeTableDto.ScheduleDto.builder()
                                    .id(schedule.getId())
                                    .name(schedule.getName())
                                    .body(schedule.getBody())
                                    .timeBitData(weekBits[day])
                                    .build());
                }
        }

        dayOfWeekIdx = startDayOfWeek;
        for (int day = 0; day <dayNum ; day++) {
            scheduleListPerDays.get(day).addAll(weekRoutines.get(dayOfWeekIdx));
            ++dayOfWeekIdx;
            dayOfWeekIdx %= WEEK_MOD;
        }

        for (Non_Periodic_Schedule schedule: nonPeriodicScheduleList ) {
            LocalDateTime scheduleStartTime = schedule.getStartTime();
            Long timeBitData = bitConverter.convertTimeToBit(scheduleStartTime, schedule.getEndTime());
            LocalDate startLocalDate = LocalDate.of(scheduleStartTime.getYear(), scheduleStartTime.getMonth(), scheduleStartTime.getDayOfMonth());
            int afterDayNumFromStart = (int) Duration.between(startDate.atStartOfDay(),startLocalDate.atStartOfDay()).toDays() +1;

            scheduleListPerDays.get(afterDayNumFromStart).add(
                    TimeTableDto.ScheduleDto.builder()
                            .id(schedule.getId())
                            .name(schedule.getName())
                            .body(schedule.getBody())
                            .timeBitData(timeBitData)
                            .build());

            Long targetBits = bitDataPerDays.get(afterDayNumFromStart);
            bitDataPerDays.set(afterDayNumFromStart,targetBits|timeBitData);
        }


        return TimeTableDto.TimeTableInfo.builder()
                .startDate(startDate)
                .endData(endDate)
                .bitListsPerDay(this.convertLongListToBitListsPerDay(bitDataPerDays))
                .scheduleListPerDays(scheduleListPerDays).build();
    }

    private long[] convertTimeStringsToBitsArray(List<String> periodicTimeStringList){
        long[] bitsArray = {0,0,0,0,0,0,0};
        for (String time: periodicTimeStringList) {
            String[] splitData = time.split(",");
            Integer dayNumber = dayToIntMap.get(splitData[0]);
            String[] duration = splitData[1].split("-");
            int startIdx = (int) (Float.parseFloat(duration[0])*2);
            int endIdx = (int) (Float.parseFloat(duration[1])*2);

            long moveBit =(1l << startIdx);
            for (int i = startIdx; i <endIdx ; i++) {
                bitsArray[dayNumber] |= moveBit;
                moveBit <<=1;
            }
        }

        return bitsArray;
    }

    private Schedule convertDtoToSchedule(TimeTableDto.SchedulePostDto schedulePostData){
        if(schedulePostData.isPeriodicType()){
            log.info("build Periodic Schedule");
            Periodic_Schedule periodicSchedule = new Periodic_Schedule();
            periodicSchedule.setWeekScheduleData(this.convertTimeStringsToBitsArray(schedulePostData.getPeriodicTimeStringList()));
            periodicSchedule.setName(schedulePostData.getName());
            periodicSchedule.setBody(schedulePostData.getBody());
            return periodicSchedule;
        }
        log.info("build NonPeriodic Schedule");
        Non_Periodic_Schedule nonPeriodicSchedule = new Non_Periodic_Schedule();
        nonPeriodicSchedule.setName(schedulePostData.getName());
        nonPeriodicSchedule.setStartTime(schedulePostData.getStartTime());
        nonPeriodicSchedule.setEndTime(schedulePostData.getEndTime());
        nonPeriodicSchedule.setBody(schedulePostData.getBody());
        return  nonPeriodicSchedule;
    }

    //멤버로 스케줄 저장
    @Transactional
    public void saveScheduleInTimeTable(Long memberId, TimeTableDto.SchedulePostDto schedulePostData){
        Member member = memberRepository.findById(memberId).get();
        Schedule schedule = this.convertDtoToSchedule(schedulePostData);

        Time_Table timeTable = member.getTimeTable();
        schedule.setTimeTable(timeTable);
        Schedule savedSchedule = scheduleRepository.save(schedule);//@JoinColumn을 가지고 있는게 주인이므로 set은 Schedule이

        List<Schedule> scheduleList = timeTable.getScheduleList();
        scheduleList.add(savedSchedule);
        timeTable.calcAllWeekScheduleData();

        log.info("add Schedule"+ savedSchedule.getId() + " to Member" + member.getId());
    }

    //멤버로 수정
    @Transactional
    public void updateScheduleInTimeTable(final Long scheduleId, TimeTableDto.SchedulePostDto updatePostData){
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(scheduleId);
        if(scheduleOptional.isEmpty()){
            log.info("NoSuchScheduleId: " + scheduleId);
            return;
        }

        Schedule schedule = scheduleOptional.get();
        if(schedule instanceof Periodic_Schedule)
            updatePostData.setPeriodicType(true);

        Schedule updateData = this.convertDtoToSchedule(updatePostData);

        if(schedule instanceof Periodic_Schedule)
            updatePeriodicSchedule((Periodic_Schedule)schedule, (Periodic_Schedule)updateData);
        else
            updateNonPeriodicSchedule((Non_Periodic_Schedule)schedule, (Non_Periodic_Schedule)updateData);

        Schedule savedSchedule = scheduleRepository.save(schedule);
        savedSchedule.getTimeTable().calcAllWeekScheduleData();
    }

    @Transactional
    private void updatePeriodicSchedule(Periodic_Schedule schedule, Periodic_Schedule updateData){
        if(updateData.getWeekScheduleData() != null)
            schedule.setWeekScheduleData(updateData.getWeekScheduleData());
        if(updateData.getName() != null)
            schedule.setName(updateData.getName());
        //일단 확인용으로 두개만
        log.info("update Schedule "+ schedule.getId() + " as " + updateData.toString());
    }

    @Transactional
    private void updateNonPeriodicSchedule(Non_Periodic_Schedule schedule, Non_Periodic_Schedule updateData){
        if(updateData.getName() != null)
            schedule.setName(updateData.getName());
        if(updateData.getStartTime() != null)
            schedule.setStartTime(updateData.getStartTime());
        if(updateData.getEndTime() != null)
            schedule.setEndTime(updateData.getEndTime());
        //일단 확인용
        log.info("update Schedule "+ schedule.getId() + " as " + updateData.toString());
    }

    @Transactional
    public void deleteScheduleInTimeTable(final Long scheduleId){
        em.flush();
        em.clear();
        Schedule schedule = scheduleRepository.findById(scheduleId).get();

        Time_Table timeTable = schedule.getTimeTable();
        timeTable.getScheduleList().remove(schedule);   //서로의 연관관계 끊기
        timeTable.calcAllWeekScheduleData();

        schedule.setTimeTable(null);                    //서로의 연관관계 끊기

        scheduleRepository.deleteById(scheduleId);
        log.info("delete Schedule "+ scheduleId);
    }

}
