package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.TimeTableDto;
import com.example.emptySaver.domain.entity.*;
import com.example.emptySaver.repository.*;
import com.example.emptySaver.utils.TimeDataSuperUltraConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleRecommendService {
    private final MemberService memberService;
    private final TimeTableService timeTableService;

    private final TimeDataSuperUltraConverter timeDataConverter;
    private final PeriodicScheduleRepository periodicScheduleRepository;
    private final MemberRepository memberRepository;
    private final NonPeriodicScheduleRepository nonPeriodicScheduleRepository;

    public List<TimeTableDto.SearchedScheduleDto> getRecommendScheduleList(final TimeTableDto.ScheduleSearchRequestForm requestForm){
        Member member = memberService.getMember();
        List<Schedule> recommendByMemberTimeTable = this.getRecommendByMemberTimeTable(member.getId(), requestForm.getStartTime(), requestForm.getEndTime());
        return timeTableService.convertScheduleListToSearchedScheduleDtoList(recommendByMemberTimeTable);
    }

    private List<Periodic_Schedule> getPeriodicScheduleListNotOverlap(final long[] memberWeekData){
        long[] notWeekData = {0,0,0,0,0,0,0};
        for (int i = 0; i < notWeekData.length; i++) {
            notWeekData[i] = ~memberWeekData[i];
        }

        List<Periodic_Schedule> includedScheduleList = new ArrayList<>();

        List<Periodic_Schedule> periodicScheduleList = periodicScheduleRepository.findByPublicType(true);
        for (Periodic_Schedule periodicSchedule : periodicScheduleList) {
            long[] weekScheduleData = periodicSchedule.getWeekScheduleData();

            boolean isOverlap = false;
            for (int i = 0; i < weekScheduleData.length; i++) { //겹치지 파악
                if(notWeekData[i] != (notWeekData[i] | weekScheduleData[i])){
                    isOverlap = true;
                    break;
                }
            }

            if(!isOverlap)
                includedScheduleList.add(periodicSchedule);
        }

        return includedScheduleList;
    }

    public List<Schedule> getRecommendByMemberTimeTable(final Long memberId, final LocalDateTime startTime, final LocalDateTime endTime){
        Member member = memberRepository.findById(memberId).get();
        Time_Table timeTable = member.getTimeTable();

        timeTable.calcAllWeekScheduleData();
        final long[] weekScheduleData = timeTable.getWeekScheduleData();

        List<Non_Periodic_Schedule> memberNonPeriodicScheduleList = new ArrayList<>();    // 멤버의 비주기 스케줄 가져옴
        List<Periodic_Schedule> memberPeriodicScheduleList = new ArrayList<>();    // 멤버의 주기 스케줄 가져옴
        List<Schedule> scheduleList = timeTable.getScheduleList();
        for (Schedule schedule : scheduleList) {
            if(schedule instanceof Non_Periodic_Schedule){
                Non_Periodic_Schedule nonPeriodicSchedule = (Non_Periodic_Schedule) schedule;
                if (nonPeriodicSchedule.getStartTime().isAfter(startTime.minusMinutes(1))
                        &&nonPeriodicSchedule.getEndTime().isBefore(endTime.plusMinutes(1)) )
                    memberNonPeriodicScheduleList.add((Non_Periodic_Schedule)schedule);
            }else{
                memberPeriodicScheduleList.add((Periodic_Schedule) schedule);
            }
        }

        List<Periodic_Schedule> passedPeriodicScheduleList = getMatchedPeriodicScheduleList(weekScheduleData, memberNonPeriodicScheduleList);

        List<Non_Periodic_Schedule> passedNonPeriodicScheduleList = getMatchedNonPeriodicScheduleList(startTime, endTime, weekScheduleData, memberNonPeriodicScheduleList);

        List<Schedule> recommendScheduleList = new ArrayList<>();
        recommendScheduleList.addAll(passedPeriodicScheduleList);
        recommendScheduleList.addAll(passedNonPeriodicScheduleList);

        return recommendScheduleList;
    }

    private List<Non_Periodic_Schedule> getMatchedNonPeriodicScheduleList(LocalDateTime startTime, LocalDateTime endTime, long[] weekScheduleData, List<Non_Periodic_Schedule> memberNonPeriodicScheduleList) {
        List<Non_Periodic_Schedule> sortMemberNonPeriodicScheduleList = memberNonPeriodicScheduleList.stream()
                .sorted(Comparator.comparing(Non_Periodic_Schedule::getStartTime).thenComparing(Non_Periodic_Schedule::getEndTime)).collect(Collectors.toList());
        List<Non_Periodic_Schedule> nonPeriodicScheduleList = nonPeriodicScheduleRepository
                .findSortByPublicTypeAndStartTimeBetween(true, startTime, endTime,
                        Sort.by(Sort.Direction.ASC, "startTime","endTime"));

        boolean[] isPassedArr = new boolean[nonPeriodicScheduleList.size()];
        int memberPointer=0, dbPointer =0;  //member 스케줄이 항상 앞에 있게 만듬
        int idx = 0;
        while(memberPointer < sortMemberNonPeriodicScheduleList.size() && dbPointer<nonPeriodicScheduleList.size()){
            Non_Periodic_Schedule memberSchedule = sortMemberNonPeriodicScheduleList.get(memberPointer);
            Non_Periodic_Schedule dbSchedule = nonPeriodicScheduleList.get(dbPointer);
            if(memberSchedule.getEndTime().isBefore(dbSchedule.getStartTime().plusMinutes(1))){ //memberSchedule이 db보다 늦게 시작 -> 절대 안 겹칩
                memberPointer++;    //memberSchedule 땡겨줌
            }else if(memberSchedule.getStartTime().isAfter(dbSchedule.getEndTime().minusMinutes(1))){   // dbSchedule이 memberSchedule보다 늦게 시작 ->안 겹침
                dbPointer++; //db Schedule을 땡겨줌
            }else{  //db스케줄이 겹치는 상황 -> 다음db로 넘어가ㅣㅁ
                isPassedArr[dbPointer] = true;
                dbPointer++;
            }
        }

        List<Non_Periodic_Schedule> matchedNonPeriodicScheduleList = new ArrayList<>();
        for (int i = 0; i < isPassedArr.length; i++) {  //슬라이드 하고 남은거
            if(!isPassedArr[i])
                matchedNonPeriodicScheduleList.add(nonPeriodicScheduleList.get(i));
        }
        log.info("non Periodic size: "+ matchedNonPeriodicScheduleList.size());

        List<Non_Periodic_Schedule> passedNonPeriodicScheduleList = new ArrayList<>();
        for (int i = 0; i < matchedNonPeriodicScheduleList.size(); i++) {
            Non_Periodic_Schedule nonPeriodicSchedule = matchedNonPeriodicScheduleList.get(i);
            int dayOfWeek = nonPeriodicSchedule.getEndTime().getDayOfWeek().getValue() -1;
            long notBits = ~weekScheduleData[dayOfWeek];
            long ret = notBits | timeDataConverter.convertTimeToBit(nonPeriodicSchedule.getStartTime(), nonPeriodicSchedule.getEndTime());

            if(ret == notBits)  //같아야 빈칸에 들어감
                passedNonPeriodicScheduleList.add(nonPeriodicSchedule);
        }
        return passedNonPeriodicScheduleList;
    }

    private List<Periodic_Schedule> getMatchedPeriodicScheduleList(long[] weekScheduleData, List<Non_Periodic_Schedule> memberNonPeriodicScheduleList) {
        List<Periodic_Schedule> passedPeriodicScheduleList = new ArrayList<>();
        List<Periodic_Schedule> periodicScheduleListNotOverlap = this.getPeriodicScheduleListNotOverlap(weekScheduleData);  // db의 주기적 스케줄 가져옴

        boolean[] isPassedArr = new boolean[periodicScheduleListNotOverlap.size()];
        for (Non_Periodic_Schedule memberNonPeriodicSchedule : memberNonPeriodicScheduleList) {
            int dayOfWeek = memberNonPeriodicSchedule.getEndTime().getDayOfWeek().getValue() -1;

            for (int i=0; i<periodicScheduleListNotOverlap.size() ; ++i) {
                if(isPassedArr[i])
                    continue;

                Periodic_Schedule periodicSchedule = periodicScheduleListNotOverlap.get(i);
                long periodicBitData = periodicSchedule.getWeekScheduleData()[dayOfWeek];

                if(periodicBitData ==0)
                    continue;

                Long nonPeriodicBitData = timeDataConverter.convertTimeToBit(memberNonPeriodicSchedule.getStartTime(), memberNonPeriodicSchedule.getEndTime());

                if((periodicBitData & nonPeriodicBitData) != 0)
                    isPassedArr[i] = true;
            }

            for (int i=0; i<periodicScheduleListNotOverlap.size() ; ++i) {
                if(!isPassedArr[i])
                    passedPeriodicScheduleList.add(periodicScheduleListNotOverlap.get(i));
            }
        }   //주기적은 모두 비교가 끝난것만 담음
        return passedPeriodicScheduleList;
    }
}
