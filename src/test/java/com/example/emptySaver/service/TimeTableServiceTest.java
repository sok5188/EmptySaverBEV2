package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.TimeTableDto;
import com.example.emptySaver.domain.entity.*;
import com.example.emptySaver.repository.MemberRepository;
import com.example.emptySaver.repository.TeamRepository;
import com.example.emptySaver.repository.TimeTableRepository;
import com.example.emptySaver.utils.TimeDataSuperUltraConverter;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TimeTableServiceTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private TimeDataSuperUltraConverter bitConverter;
    @Autowired
    private TimeTableService timeTableService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TimeTableRepository timeTableRepository;
    @Autowired
    private TeamRepository teamRepository;

    private TimeTableDto.SchedulePostDto getTempSchedulePostDto(){
        List<String> timeList = Arrays.asList("화,0.5-1.5","화,8.5-9.5","금,19-24");
        //long[] weekData = {0,100,100,100,0,0,0};
        TimeTableDto.SchedulePostDto periodicSchedule = TimeTableDto.SchedulePostDto.builder().name("캡스톤").periodicType("true").periodicTimeStringList(timeList).build();
        return periodicSchedule;
    }

    private TimeTableDto.SchedulePostDto getNonPeriodicSchedulePostDto(){
        TimeTableDto.SchedulePostDto nonPeriodicSchedule = TimeTableDto.SchedulePostDto.builder().name("캡스톤").periodicType("false")
                .startTime(LocalDateTime.of(2023,05,07,8,30,0))
                .endTime(LocalDateTime.of(2023,05,07,9,30,0))
                .build();
        return nonPeriodicSchedule;
    }

    @Test
    void 팀의_스케줄_받아오기(){
        Time_Table timeTable = Time_Table.builder().title("let go").weekScheduleData(new long[]{0l,0l,0l,0l,0l,0l,0l}).build();
        Time_Table savedTable = timeTableRepository.save(timeTable);

        Team team = new Team();
        team.setTimeTable(savedTable);
        Team savedTeam = teamRepository.save(team);

        Team findTeam = teamRepository.findById(savedTeam.getId()).get();

        TimeTableDto.SchedulePostDto schedulePostDto = getTempSchedulePostDto();
        TimeTableDto.SchedulePostDto nonPeriodicSchedulePostDto = getNonPeriodicSchedulePostDto();
        timeTableService.saveScheduleByTeam(findTeam.getId(),schedulePostDto);
        timeTableService.saveScheduleByTeam(findTeam.getId(),nonPeriodicSchedulePostDto);

        List<TimeTableDto.TeamScheduleDto> teamScheduleList = timeTableService.getTeamScheduleList(findTeam.getId());

        assertThat(teamScheduleList.size()).isEqualTo(2);

        System.out.println(teamScheduleList.get(0).getTimeData());
        System.out.println(teamScheduleList.get(1).getTimeData());
    }

    @Test
    void TimeTableDto받아오기(){
        Time_Table timeTable = Time_Table.builder().title("육사시미").weekScheduleData(new long[]{0l,0l,0l,0l,0l,0l,0l}).build();
        Time_Table savedTable = timeTableRepository.save(timeTable);

        Member member = Member.init().name("멤버").build();
        member.setTimeTable(savedTable);
        Member savedMember = memberRepository.save(member);

        List<String> timeList1 = Arrays.asList("화,0.5-1.5","화,18-19","금,19-24");
        TimeTableDto.SchedulePostDto periodicSchedule1 = TimeTableDto.SchedulePostDto.builder().name("캡스톤").periodicType("true").periodicTimeStringList(timeList1).build();
        timeTableService.saveScheduleInTimeTable(savedMember.getId(), periodicSchedule1);

        List<String> timeList2 = Arrays.asList("금,14-17","수,18-18.5");
        TimeTableDto.SchedulePostDto periodicSchedule2 = TimeTableDto.SchedulePostDto.builder().name("z스톤").periodicType("true").periodicTimeStringList(timeList2).build();
        timeTableService.saveScheduleInTimeTable(savedMember.getId(), periodicSchedule2);

        LocalDateTime startDate = LocalDateTime.of(2023, 4, 30,10,0);
        LocalDateTime endDate = LocalDateTime.of(2023, 5, 10,12,30);
        TimeTableDto.TimeTableInfo timeTableDto = timeTableService.getMemberTimeTableByDayNum(savedMember.getId(), startDate.toLocalDate(), endDate.toLocalDate());


        List<List<TimeTableDto.ScheduleDto>> scheduleListPerDays = timeTableDto.getScheduleListPerDays();
        for (List<TimeTableDto.ScheduleDto> ss: scheduleListPerDays) {
            System.out.println("======");
            for (TimeTableDto.ScheduleDto scheduleDto: ss) {
                System.out.println(scheduleDto);

                System.out.println(scheduleDto.getTimeStringData());
                //System.out.println("bits: "+ Long.toBinaryString(scheduleDto.getTimeBitData()));
            }
        }
    }

    @Test
    void LocalTime_ToBit_연산_테스트(){
        LocalDateTime startDate = LocalDateTime.of(2023, 4, 30,10,0);
        LocalDateTime endDate = LocalDateTime.of(2023, 4, 30,12,30);
        Long toBit = bitConverter.convertTimeToBit(startDate, endDate);
        System.out.println(Long.toBinaryString(toBit));
    }


    @Transactional
    private Member getSavedMember(){
        Time_Table timeTable = Time_Table.builder().title("육사시미").build();
        em.persist(timeTable);

        Member member = Member.init().name("멤버").build();
        member.setTimeTable(timeTable);
        em.persist(member);
        em.flush();     //저장시킴

        return(memberRepository.findById(member.getId()).get());   //저장된 멤버 호출
    }

    @Transactional
    @Test
    void 스케줄삭제(){
        Member savedMember = getSavedMember();      //영속성 유지됨

        TimeTableDto.SchedulePostDto periodicSchedule = getTempSchedulePostDto();

        timeTableService.saveScheduleInTimeTable(savedMember.getId(), periodicSchedule);        //멤버에게 스케줄 저장

        em.flush();
            //em.clear();

        Long scheduleId = memberRepository.findById(savedMember.getId()).get().getTimeTable().getScheduleList().get(0).getId();
        timeTableService.deleteScheduleInTimeTable(scheduleId); //삭제
        em.flush();     //이런거 다 삭제할땐 영속성이 오히려 불편해져서 넣는거
        em.clear();

        Member finalMember = memberRepository.findById(savedMember.getId()).get();
        assertThat(finalMember.getTimeTable().getScheduleList().size()).isEqualTo(0);
        assertThat(finalMember.getTimeTable().getWeekScheduleData()[1]).isEqualTo(0l);
    }

    @Transactional
    @Test
    void Id로스케줄수정(){
        Member savedMember = getSavedMember();

        TimeTableDto.SchedulePostDto periodicSchedule = getTempSchedulePostDto();
        timeTableService.saveScheduleInTimeTable(savedMember.getId(), periodicSchedule);        //멤버에게 스케줄 저장

        Long scheduleId = savedMember.getTimeTable().getScheduleList().get(0).getId();

        //long[] newWeekData = {100,0,0,0,0,100,0};
        List<String> timeList = Arrays.asList("금,14-17","수,18-18.5");
        TimeTableDto.SchedulePostDto newPeriodicSchedule = TimeTableDto.SchedulePostDto.builder().name("킹스톤").periodicType("true").periodicTimeStringList(timeList).build();

        timeTableService.updateScheduleInTimeTable(scheduleId, newPeriodicSchedule);

        List<Schedule> scheduleList = savedMember.getTimeTable().getScheduleList();
        //for (Schedule sc: scheduleList)
            //System.out.println("schedule: "+sc);

        Periodic_Schedule updatedSchedule = (Periodic_Schedule) scheduleList.get(0);
        System.out.println("bits: "+ Long.toBinaryString(updatedSchedule.getWeekScheduleData()[2]));
        //assertThat(updatedSchedule.getWeekScheduleData()).isEqualTo();
        assertThat(updatedSchedule.getName()).isEqualTo(newPeriodicSchedule.getName());
        //assertThat(savedMember.getTimeTable().getWeekScheduleData()).isEqualTo(newWeekData);
        em.flush();     //왜 이거 없을때는 update쿼리가 안나감? Transactionl 끝나면 나가야되는거 아님? 롤백 때문임?
    }

    @Transactional
    @Test
    void 멤버타임테이블에스케줄저장(){
        Time_Table timeTable = Time_Table.builder().title("육사시미").build();
        em.persist(timeTable);

        Member member = Member.init().name("멤버").build();
        member.setTimeTable(timeTable);
        em.persist(member);

        em.flush();     //저장시킴
        em.clear();

        Member savedMember = memberRepository.findById(member.getId()).get();   //저장된 멤버 호출

        TimeTableDto.SchedulePostDto periodicSchedule = getTempSchedulePostDto();
        timeTableService.saveScheduleInTimeTable(savedMember.getId(), periodicSchedule);        //멤버에게 스케줄 저장
        em.flush();
        em.clear();

        savedMember = memberRepository.findById(member.getId()).get();
        assertThat(savedMember.getTimeTable().getScheduleList().size()).isEqualTo(1);   //저장 확인
        assertThat(savedMember.getTimeTable().getScheduleList().get(0).getName()).isEqualTo(periodicSchedule.getName());   //저장 내용 확인
        assertThat(savedMember.getTimeTable().getScheduleList().get(0).getTimeTable().getId()).isEqualTo(timeTable.getId());   //스케줄과 timeTable의 연관관계 확인
        Periodic_Schedule periodicSchedule1 = (Periodic_Schedule) savedMember.getTimeTable().getScheduleList().get(0);
        System.out.println(Long.toBinaryString(periodicSchedule1.getWeekScheduleData()[1]));
    }

    @Transactional
    @Test
    void 멤버타임테이블에주기적스케줄저장(){
        Time_Table timeTable = Time_Table.builder().title("육사시미").build();
        em.persist(timeTable);

        Member member = Member.init().name("멤버").build();
        member.setTimeTable(timeTable);
        em.persist(member);

        em.flush();     //저장시킴
        em.clear();

        Member savedMember = memberRepository.findById(member.getId()).get();   //저장된 멤버 호출

        TimeTableDto.SchedulePostDto periodicSchedule = getTempSchedulePostDto();
        timeTableService.saveScheduleInTimeTable(savedMember.getId(), periodicSchedule);        //멤버에게 스케줄 저장
        em.flush();
        em.clear();

        savedMember = memberRepository.findById(member.getId()).get();
        assertThat(savedMember.getTimeTable().getScheduleList().size()).isEqualTo(1);   //저장 확인
        assertThat(savedMember.getTimeTable().getScheduleList().get(0).getName()).isEqualTo(periodicSchedule.getName());   //저장 내용 확인
        assertThat(savedMember.getTimeTable().getScheduleList().get(0).getTimeTable().getId()).isEqualTo(timeTable.getId());   //스케줄과 timeTable의 연관관계 확인

        //assertThat(savedMember.getTimeTable().getWeekScheduleData()).isEqualTo(new long[]{0,100,100,100,0,0,0});   //bitData 재계산 확인
    }
}