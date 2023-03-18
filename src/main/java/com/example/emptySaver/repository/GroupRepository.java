package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Team,Long> {
}
