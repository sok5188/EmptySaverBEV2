package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member_Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberGroupRepository extends JpaRepository<Member_Team,Long> {
}
