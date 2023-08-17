package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
//    @Cacheable(cacheNames = "member",keyGenerator = "myCustomKeyGenerator")
    Optional<Member> findFirstByUsername(String username);
    Optional<Member> findFirstByEmail(String email);
    List<Member> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.email=:email")
    List<Member> findByEmailPessimistic(String email);
}
