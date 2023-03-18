package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Schedule;
import com.example.emptySaver.domain.entity.Time_Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class TimeTableRepositoryTest {
    @Autowired
    private TimeTableRepository timeTableRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    @BeforeEach
    void beforeEach(){
        timeTableRepository.deleteAll();
        scheduleRepository.deleteAll();
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


    @DisplayName("schedule과 timetable간의 관계 저장 테스트")
    @Test
    void testRelationWithScheduleAndTimeTable(){
        Time_Table table = Time_Table.builder().title("noSoEasy").build();
        Time_Table savedTable = timeTableRepository.save(table);

        Schedule schedule1 = Schedule.builder().name("캡스톤").timeTable(table).build();
        Schedule schedule2 = Schedule.builder().name("컴파일러").timeTable(table).build();
        Schedule savedSchedule1 = scheduleRepository.save(schedule1);
        Schedule savedSchedule2 = scheduleRepository.save(schedule2);

        //then
        List<Schedule> scheduleList = timeTableRepository.findById(savedTable.getId()).get().getScheduleList();

        for(Schedule schedule:scheduleList){
            System.out.println(schedule);
        }
        assertThat(scheduleList.size()).isEqualTo(2);
    }
}