package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Periodic_Schedule;
import com.example.emptySaver.domain.entity.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ScheduleRepositoryTest {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @BeforeEach
    void beforeEach(){
        scheduleRepository.deleteAll();
    }

    @DisplayName("Schedule 상속 테스트")
    @Test
    void testSaveWithInheritance(){
        long[] weekData = {0,1,1};

        Periodic_Schedule periodicSchedule = new Periodic_Schedule();
        periodicSchedule.setName("욕망");
        periodicSchedule.setWeekScheduleData(weekData);

        Periodic_Schedule savedSchedule = scheduleRepository.save(periodicSchedule);
        Schedule upperClass = savedSchedule;

        assertThat(upperClass.getId()).isEqualTo(savedSchedule.getId());

        for(long v:savedSchedule.getWeekScheduleData()){
            System.out.println(v);
        }
    }

}