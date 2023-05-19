package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.Periodic_Schedule;
import com.example.emptySaver.domain.entity.Schedule;
import com.example.emptySaver.domain.entity.Time_Table;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TimeTableRepositoryTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private TimeTableRepository timeTableRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MemberRepository memberRepository;
/*
    @BeforeEach
    void afterEach(){
        em.flush();
        em.clear();
        timeTableRepository.deleteAll();
        timeTableRepository.flush();

        scheduleRepository.deleteAll();
        memberRepository.deleteAll();
    }*/

    @Transactional
    private Long temp(){
        Member member = new Member();
        member.setName("오운완");
        em.persist(member);

        Time_Table timeTable = Time_Table.builder().title("헬스루틴").member(member).build();
        em.persist(timeTable);
        member.setTimeTable(timeTable);

        //Member savedMember = memberRepository.save(member);

        em.flush();
        em.clear();
        //then
        Long memberId = member.getId();
        Long timeTableId = timeTable.getId();
        memberRepository.deleteById(memberId);

        assertThat(memberRepository.findById(memberId).isEmpty()).isTrue();

        return timeTableId;
    }


    @DisplayName("멤버삭제시 시간표도 삭제되는지 확인")
    @Test
    void deleteMemberWithTimeTable(){
        //assertThat(memberRepository.findById(temp()).isEmpty()).isTrue();
    }

    @Transactional
    @Test
    void 멤버에시간표저장(){
        Member member = new Member();
        member.setName("오운완");
        em.persist(member);

        Time_Table timeTable = Time_Table.builder().title("헬스루틴").member(member).build();
        em.persist(timeTable);

        //Member savedMember = memberRepository.save(member);

        em.flush();
        em.clear();
        //then
        Member savedMember = memberRepository.findById(member.getId()).get();
        Time_Table savedTimeTable = timeTableRepository.findById(timeTable.getId()).get();
        assertThat(savedMember.getTimeTable().getId()).isEqualTo(timeTable.getId());
        assertThat(savedTimeTable.getMember().getId()).isEqualTo(savedMember.getId());
    }

    @Transactional
    @DisplayName("weekScheduleData 비트 연산 테스트")
    @Test
    void addScheduleData(){
        long data1 = 15;
        long data2 = data1<<4;
        long result = data1 | data2;

        Time_Table table = Time_Table.builder().title("headHigh").build();
        Time_Table savedTable = timeTableRepository.save(table);
        em.flush();
        em.clear();

        Periodic_Schedule periodicSchedule1 = new Periodic_Schedule();
        periodicSchedule1.setWeekScheduleData(new long[]{0,data1,0,0,data1,0,data2});
        periodicSchedule1.setTimeTable(savedTable);
        scheduleRepository.save(periodicSchedule1);

        Periodic_Schedule periodicSchedule2 = new Periodic_Schedule();
        periodicSchedule2.setWeekScheduleData(new long[]{0,data1,0,0,data2,0,data1});
        periodicSchedule2.setTimeTable(savedTable);
        scheduleRepository.save(periodicSchedule2);

        em.flush();
        em.clear();
        
        Time_Table time_table = timeTableRepository.findById(savedTable.getId()).get();
        time_table.calcAllWeekScheduleData();
        long[] weekScheduleData = time_table.getWeekScheduleData();

        assertThat(weekScheduleData[1]).isEqualTo(data1);
        assertThat(weekScheduleData[4]).isEqualTo(result);
        assertThat(weekScheduleData[6]).isEqualTo(result);

    }

    @DisplayName("timeTable save test")
    @Test
    void saveTimeTable(){
        Time_Table table = Time_Table.builder().title("noSoEasy").build();
        Time_Table savedTable = timeTableRepository.save(table);

        assertThat(savedTable.getTitle()).isEqualTo(savedTable.getTitle());
    }

    @DisplayName("schedule을 timetable과 저장")
    @Test
    void saveScheduleWithTimeTable(){
        Time_Table table = Time_Table.builder().title("noSoEasy").build();
        Time_Table savedTable = timeTableRepository.save(table);

        Schedule schedule = Schedule.builder().name("캡스톤").timeTable(table).build();
        Schedule savedSchedule = scheduleRepository.save(schedule);

        assertThat(scheduleRepository.findById(savedSchedule.getId()).get().getTimeTable().getId())
                .isEqualTo(savedTable.getId());
    }


    @Transactional
    @DisplayName("schedule과 timetable간의 관계 저장 테스트")
    @Test
    void testRelationWithScheduleAndTimeTable(){
        Time_Table table = Time_Table.builder().title("noSoEasy").build();
        Time_Table savedTable = timeTableRepository.save(table);
        em.flush();
        em.clear();

        Schedule schedule1 = Schedule.builder().name("캡스톤").timeTable(table).build();
        Schedule schedule2 = Schedule.builder().name("컴파일러").timeTable(table).build();
        Schedule savedSchedule1 = scheduleRepository.save(schedule1);
        Schedule savedSchedule2 = scheduleRepository.save(schedule2);

        em.flush();
        em.clear();
        //then
        List<Schedule> scheduleList = timeTableRepository.findById(savedTable.getId()).get().getScheduleList();

        for(Schedule schedule:scheduleList){
            System.out.println(schedule);
        }
        assertThat(scheduleList.size()).isEqualTo(2);
    }
/*
    @DisplayName("LocalDateTime DB 저장 테스트")
    @Test
    void saveWithTimeData(){
        LocalDateTime now = LocalDateTime.now();
        Schedule schedule = Schedule.builder().name("컴파일러").startTime(now).build();
        Schedule savedSchedule = scheduleRepository.save(schedule);

        System.out.println(savedSchedule);
        assertThat(savedSchedule.getStartTime()).isEqualTo(now);
    }*/
}