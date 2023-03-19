package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Periodic_Schedule;
import com.example.emptySaver.domain.entity.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
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
        int[][] weekData = {{0,0,0},{1,1,1}};

        Periodic_Schedule periodicSchedule = new Periodic_Schedule();
        periodicSchedule.setName("욕망");
        periodicSchedule.setWeekScheduleData(weekData);

        Periodic_Schedule savedSchedule = scheduleRepository.save(periodicSchedule);
        Schedule upperClass = savedSchedule;

        assertThat(upperClass.getId()).isEqualTo(savedSchedule.getId());

        for(int[] list:savedSchedule.getWeekScheduleData()){
            for(int v: list){
                System.out.println(v);
            }
        }
    }

}