package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Cacheable(cacheNames = "member",keyGenerator = "myCustomKeyGenerator")
    Optional<Member> findFirstByUsername(String username);
    Optional<Member> findFirstByEmail(String email);


}
