package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Non_Periodic_Schedule;
import com.example.emptySaver.domain.entity.Periodic_Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PeriodicScheduleRepository extends JpaRepository<Periodic_Schedule, Long> {

    List<Periodic_Schedule> findByPublicType(boolean publicType);
}
