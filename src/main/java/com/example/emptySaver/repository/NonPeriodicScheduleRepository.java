package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Non_Periodic_Schedule;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NonPeriodicScheduleRepository extends JpaRepository<Non_Periodic_Schedule, Long> {

    //List<Schedule> findByPublicType(boolean publicType);
    List<Non_Periodic_Schedule> findByPublicTypeAndStartTimeBetween(boolean publicType,LocalDateTime from, LocalDateTime to);
    List<Non_Periodic_Schedule> findSortByPublicTypeAndStartTimeBetween(boolean publicType, LocalDateTime from, LocalDateTime to, Sort sort);

}
