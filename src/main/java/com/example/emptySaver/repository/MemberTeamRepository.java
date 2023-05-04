package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.MemberTeam;
import com.example.emptySaver.domain.entity.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface MemberTeamRepository extends JpaRepository<MemberTeam,Long> {

    //By Team
    List<MemberTeam> findByTeam(Team team);
    @EntityGraph(attributePaths = "member")
    List<MemberTeam> findWithMemberByTeam(Team team);

    List<MemberTeam> findByMember(Member member);
    @EntityGraph(attributePaths = "team")
    List<MemberTeam> findWithTeamByMember(Member member);

    int countByTeam(Team team);


}
