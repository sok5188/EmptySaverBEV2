package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member_Interest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberInterestRepository extends JpaRepository<Member_Interest,Long> {
}
