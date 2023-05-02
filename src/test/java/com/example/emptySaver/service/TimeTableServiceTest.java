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

import java.util.List;

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

        long[] weekData = {0,100,100,0,0,0,0};
        Periodic_Schedule periodicSchedule = new Periodic_Schedule();           //저장시킬 스케줄
        periodicSchedule.setName("캡스톤");
        periodicSchedule.setWeekScheduleData(weekData);

        timeTableService.saveScheduleInTimeTable(periodicSchedule,savedMember);        //멤버에게 스케줄 저장

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

        long[] weekData = {0,100,100,0,0,0,0};
        Periodic_Schedule periodicSchedule = new Periodic_Schedule();           //저장시킬 스케줄
        periodicSchedule.setName("캡스톤");
        periodicSchedule.setWeekScheduleData(weekData);

        timeTableService.saveScheduleInTimeTable(periodicSchedule,savedMember);        //멤버에게 스케줄 저장

        Long scheduleId = savedMember.getTimeTable().getScheduleList().get(0).getId();

        long[] newWeekData = {100,0,0,0,0,100,0};
        Periodic_Schedule newPeriodicSchedule = new Periodic_Schedule();           //새로운 스케줄 데이터
        newPeriodicSchedule.setName("코코롱");
        newPeriodicSchedule.setWeekScheduleData(newWeekData);

        timeTableService.updateScheduleInTimeTable(scheduleId, newPeriodicSchedule);

        List<Schedule> scheduleList = savedMember.getTimeTable().getScheduleList();
        //for (Schedule sc: scheduleList)
            //System.out.println("schedule: "+sc);

        Periodic_Schedule updatedSchedule = (Periodic_Schedule) scheduleList.get(0);
        assertThat(updatedSchedule.getWeekScheduleData()).isEqualTo(newPeriodicSchedule.getWeekScheduleData());
        assertThat(updatedSchedule.getName()).isEqualTo(newPeriodicSchedule.getName());
        assertThat(savedMember.getTimeTable().getWeekScheduleData()).isEqualTo(newWeekData);
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