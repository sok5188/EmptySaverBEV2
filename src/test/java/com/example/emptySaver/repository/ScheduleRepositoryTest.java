package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Non_Periodic_Schedule;
import com.example.emptySaver.domain.entity.Periodic_Schedule;
import com.example.emptySaver.domain.entity.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

//@SpringBootTest
//@ActiveProfiles("test")
@DataJpaTest
class ScheduleRepositoryTest {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private NonPeriodicScheduleRepository nonPeriodicScheduleRepository;

    @BeforeEach
    void beforeEach(){
        scheduleRepository.deleteAll();
    }

    @DisplayName("LocalDateTime으로_queryMethod활용하기")
    @Test
    void testQueryMethodWithLocalDateTime(){
        long[] weekData = {0,100,100,0,0,0,0};

        Periodic_Schedule periodicSchedule = new Periodic_Schedule();
        periodicSchedule.setName("욕망");
        periodicSchedule.setWeekScheduleData(weekData);
        Periodic_Schedule savedPeriodicSchedule = scheduleRepository.save(periodicSchedule);

        LocalDateTime startTime = LocalDateTime.of(2023, 05, 07, 8, 30, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 05, 07, 9, 30, 0);
        Non_Periodic_Schedule nonPeriodicSchedule = new Non_Periodic_Schedule();
        nonPeriodicSchedule.setName("헤겔");
        nonPeriodicSchedule.setStartTime(startTime);
        nonPeriodicSchedule.setEndTime(endTime);
        Non_Periodic_Schedule savedNonPeriodicSchedule = scheduleRepository.save(nonPeriodicSchedule);

        List<Non_Periodic_Schedule> searchedList = nonPeriodicScheduleRepository.findByPublicTypeAndStartTimeBetween(false,startTime, endTime);
        assertThat(searchedList.size()).isEqualTo(1);
        System.out.println(searchedList.get(0).toString());

    }

    @DisplayName("Schedule 상속 테스트")
    @Test
    void testSaveWithInheritance(){
        long[] weekData = {0,1,1};

        Periodic_Schedule periodicSchedule = new Periodic_Schedule();
        periodicSchedule.setName("욕망");
        periodicSchedule.setWeekScheduleData(weekData);
        periodicSchedule.setPublicType(true);

        Periodic_Schedule savedSchedule = scheduleRepository.save(periodicSchedule);
        Schedule upperClass = savedSchedule;
        System.out.println(upperClass.isPublicType());
        assertThat(upperClass.isPublicType()).isTrue();
        assertThat(upperClass.getId()).isEqualTo(savedSchedule.getId());

        for(long v:savedSchedule.getWeekScheduleData()){
            System.out.println(v);
        }
    }

}