package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Time_Table;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeTableRepository extends JpaRepository<Time_Table,Long> {
}
