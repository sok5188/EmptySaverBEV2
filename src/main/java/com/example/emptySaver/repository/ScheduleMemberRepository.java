package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.Schedule;
import com.example.emptySaver.domain.entity.ScheduleMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleMemberRepository extends JpaRepository<ScheduleMember, Long> {
    Optional<ScheduleMember> findByMemberAndSchedule(Member member, Schedule schedule);
}
