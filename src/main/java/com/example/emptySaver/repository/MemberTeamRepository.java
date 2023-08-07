package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.MemberTeam;
import com.example.emptySaver.domain.entity.Team;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberTeamRepository extends JpaRepository<MemberTeam,Long> {

    //By Team
    List<MemberTeam> findByTeam(Team team);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from MemberTeam t where t.team=:team")
    List<MemberTeam> findByTeamPessimistic(Team team);
    @EntityGraph(attributePaths = "member")
    List<MemberTeam> findWithMemberByTeam(Team team);

    List<MemberTeam> findByMember(Member member);
    @EntityGraph(attributePaths = "team")
    List<MemberTeam> findWithTeamByMember(Member member);

    int countByTeam(Team team);

    Optional<MemberTeam> findFirstByMemberAndTeam(Member member, Team team);


}
