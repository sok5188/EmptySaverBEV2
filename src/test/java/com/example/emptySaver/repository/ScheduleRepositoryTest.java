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
import org.springframework.data.domain.Sort;
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


    @DisplayName("비주기 스케줄 정렬해서 검색하기")
    @Test
    void sortNonPeriodicSchedule(){
        LocalDateTime startDate1 = LocalDateTime.of(2023, 4, 30,10,0);
        LocalDateTime endDate1 = LocalDateTime.of(2023, 4, 30,12,30);
        Non_Periodic_Schedule schedule1 = new Non_Periodic_Schedule();
        schedule1.setName("2");
        schedule1.setPublicType(true);
        schedule1.setStartTime(startDate1);
        schedule1.setEndTime(endDate1);

        scheduleRepository.save(schedule1);

        LocalDateTime startDate2 = LocalDateTime.of(2023, 4, 30,9,0);
        LocalDateTime endDate2 = LocalDateTime.of(2023, 4, 30,12,40);
        Non_Periodic_Schedule schedule2 = new Non_Periodic_Schedule();
        schedule2.setName("1");
        schedule2.setPublicType(true);
        schedule2.setStartTime(startDate2);
        schedule2.setEndTime(endDate2);


        scheduleRepository.save(schedule2);

        LocalDateTime startDate3 = LocalDateTime.of(2023, 5, 30,9,0);
        LocalDateTime endDate3 = LocalDateTime.of(2023, 5, 30,12,40);
        Non_Periodic_Schedule schedule3 = new Non_Periodic_Schedule();
        schedule3.setName("3");
        schedule3.setPublicType(true);
        schedule3.setStartTime(startDate3);
        schedule3.setEndTime(endDate3);

        scheduleRepository.save(schedule3);

        List<Non_Periodic_Schedule> sortByPublicTypeAndStartTimeBetween = nonPeriodicScheduleRepository.findSortByPublicTypeAndStartTimeBetween(true, startDate2, endDate3, Sort.by(Sort.Direction.ASC, "startTime", "endTime"));

        for (Non_Periodic_Schedule nonPeriodicSchedule : sortByPublicTypeAndStartTimeBetween) {
            System.out.println(nonPeriodicSchedule);
        }

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