package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findFirstByUsername(String username);
    Optional<Member> findFirstByEmail(String email);


}
