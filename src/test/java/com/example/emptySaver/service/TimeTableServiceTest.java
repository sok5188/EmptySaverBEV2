package com.example.emptySaver.service;

import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.Periodic_Schedule;
import com.example.emptySaver.domain.entity.Schedule;
import com.example.emptySaver.domain.entity.Time_Table;
import com.example.emptySaver.repository.MemberRepository;
import com.example.emptySaver.repository.ScheduleRepository;
import com.example.emptySaver.repository.TimeTableRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TimeTableServiceTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private TimeTableService timeTableService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TimeTableRepository timeTableRepository;

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

        Schedule schedule = Schedule.builder().name("결전의날").build();        //저장시킬 스케줄
        timeTableService.saveScheduleInTimeTable(schedule,savedMember);        //멤버에게 스케줄 저장
        em.flush();
        em.clear();

        savedMember = memberRepository.findById(member.getId()).get();
        assertThat(savedMember.getTimeTable().getScheduleList().size()).isEqualTo(1);   //저장 확인
        assertThat(savedMember.getTimeTable().getScheduleList().get(0).getName()).isEqualTo(schedule.getName());   //저장 내용 확인
        assertThat(savedMember.getTimeTable().getScheduleList().get(0).getTimeTable().getId()).isEqualTo(timeTable.getId());   //스케줄과 timeTable의 연관관계 확인
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

        long[] weekData = {0,100,100,0,0,0,0};
        Periodic_Schedule periodicSchedule = new Periodic_Schedule();           //저장시킬 스케줄
        periodicSchedule.setName("캡스톤");
        periodicSchedule.setWeekScheduleData(weekData);

        timeTableService.saveScheduleInTimeTable(periodicSchedule,savedMember);        //멤버에게 스케줄 저장
        em.flush();
        em.clear();

        savedMember = memberRepository.findById(member.getId()).get();
        assertThat(savedMember.getTimeTable().getScheduleList().size()).isEqualTo(1);   //저장 확인
        assertThat(savedMember.getTimeTable().getScheduleList().get(0).getName()).isEqualTo(periodicSchedule.getName());   //저장 내용 확인
        assertThat(savedMember.getTimeTable().getScheduleList().get(0).getTimeTable().getId()).isEqualTo(timeTable.getId());   //스케줄과 timeTable의 연관관계 확인

        assertThat(savedMember.getTimeTable().getWeekScheduleData()).isEqualTo(weekData);   //bitData 재계산 확인
    }
}