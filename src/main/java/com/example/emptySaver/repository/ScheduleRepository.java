package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByPublicType(boolean publicType);
    List<Schedule> findByOriginScheduleId(Long originScheduleId);
}
