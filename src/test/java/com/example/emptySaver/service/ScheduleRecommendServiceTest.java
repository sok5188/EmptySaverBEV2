package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.TimeTableDto;
import com.example.emptySaver.domain.entity.*;
import com.example.emptySaver.repository.MemberRepository;
import com.example.emptySaver.repository.ScheduleRepository;
import com.example.emptySaver.repository.TeamRepository;
import com.example.emptySaver.repository.TimeTableRepository;
import com.example.emptySaver.service.impl.TimeTableServiceImpl;
import com.example.emptySaver.utils.TimeDataSuperUltraConverter;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ScheduleRecommendServiceTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private TimeDataSuperUltraConverter timeDataConverter;
    @Autowired
    private TimeTableServiceImpl timeTableService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ScheduleRecommendService recommendService;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TimeTableRepository timeTableRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private TeamRepository teamRepository;

    private TimeTableDto.SchedulePostDto getNonPeriodicSchedulePostDto(int day,int hour,int minute,int until){
        TimeTableDto.SchedulePostDto nonPeriodicSchedule = TimeTableDto.SchedulePostDto.builder().name("네온스톤").periodicType("false")
                .startTime(LocalDateTime.of(2023,05,day,hour,minute,0))
                .endTime(LocalDateTime.of(2023,05,day,hour +until,minute,0))
                .build();
        return nonPeriodicSchedule;
    }

    private TimeTableDto.SchedulePostDto getTempSchedulePostDto(String[] timeData){
        List<String> timeList = Arrays.asList(timeData);
        //long[] weekData = {0,100,100,100,0,0,0};
        TimeTableDto.SchedulePostDto periodicSchedule = TimeTableDto.SchedulePostDto.builder().name("캡스톤").periodicType("true").periodicTimeStringList(timeList).build();
        return periodicSchedule;
    }

    @Transactional
    @DisplayName("추천 동작 확인")
    @Test
    void recommendTest(){
        Time_Table timeTable = Time_Table.builder().title("육사시미").build();
        em.persist(timeTable);

        Member member = Member.init().name("멤버").build();
        member.setTimeTable(timeTable);
        em.persist(member);

        em.flush();     //저장시킴
        em.clear();

        Member savedMember = memberRepository.findById(member.getId()).get();   //저장된 멤버 호출

        TimeTableDto.SchedulePostDto periodicSchedule = getTempSchedulePostDto(new String[] {"화,9:00-15:00","수,9:00-15:00","금,9:00-15:00","일,9:00-15:00"});
        timeTableService.saveScheduleInTimeTable(savedMember.getId(), periodicSchedule);        //멤버에게 스케줄 저장
        TimeTableDto.SchedulePostDto nonPeriodicSchedulePostDto = this.getNonPeriodicSchedulePostDto(18,9,0,6);
        timeTableService.saveScheduleInTimeTable(savedMember.getId(), nonPeriodicSchedulePostDto);        //멤버에게 스케줄 저장
        em.flush();
        em.clear();

        Periodic_Schedule publicPeriodicSchedule1 = new Periodic_Schedule();
        publicPeriodicSchedule1.setName("멤버의 비주기와 겹치는 주기");
        publicPeriodicSchedule1.setPublicType(true);
        publicPeriodicSchedule1.setWeekScheduleData(new long[]{0,0,0,
                timeDataConverter.convertTimeToBit(LocalDateTime.of(2023,05,16,10,0),LocalDateTime.of(2023,05,16,18,0)),
                0,0,0});    //"목,10:00-18:00"

        scheduleRepository.save(publicPeriodicSchedule1);


        Periodic_Schedule publicPeriodicSchedule2 = new Periodic_Schedule();
        publicPeriodicSchedule2.setName("안겹치는 주기");
        publicPeriodicSchedule2.setPublicType(true);
        publicPeriodicSchedule2.setWeekScheduleData(new long[]{
                timeDataConverter.convertTimeToBit(LocalDateTime.of(2023,05,16,10,0),LocalDateTime.of(2023,05,16,14,0)),
                0,0,0,0,0,0});    //"월,10:00-14:00"

        scheduleRepository.save(publicPeriodicSchedule2);


        Non_Periodic_Schedule publicNonPeriodicSchedule1 = new Non_Periodic_Schedule();
        publicNonPeriodicSchedule1.setName("멤버의 비주기와 겹치는 비주기");
        publicNonPeriodicSchedule1.setPublicType(true);
        publicNonPeriodicSchedule1.setStartTime(LocalDateTime.of(2023,05,18,9,30));
        publicNonPeriodicSchedule1.setEndTime(LocalDateTime.of(2023,05,18,11,0));


        scheduleRepository.save(publicNonPeriodicSchedule1);

        Non_Periodic_Schedule publicNonPeriodicSchedule2 = new Non_Periodic_Schedule();
        publicNonPeriodicSchedule2.setName("안겹치는 비주기");
        publicNonPeriodicSchedule2.setPublicType(true);
        publicNonPeriodicSchedule2.setStartTime(LocalDateTime.of(2023,05,16,18,0));
        publicNonPeriodicSchedule2.setEndTime(LocalDateTime.of(2023,05,16,20,0));


        scheduleRepository.save(publicNonPeriodicSchedule2);


        List<Schedule> recommendByMemberTimeTable = recommendService.getRecommendByMemberTimeTable(member.getId(),
                LocalDateTime.of(2023, 05, 15, 0, 0), LocalDateTime.of(2023, 05, 28, 0, 0));


        for (Schedule schedule : recommendByMemberTimeTable) {
            System.out.println(schedule.getName());
        }


    }
}