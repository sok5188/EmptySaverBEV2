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

    //By Owner
    List<MemberTeam> findByOwner(Member owner);
    @EntityGraph(attributePaths = "member")
    List<MemberTeam> findWithMemberByOwner(Member owner);
    @EntityGraph(attributePaths = "team")
    List<MemberTeam> findWithTeamByOwner(Member owner);
    @EntityGraph(attributePaths = {"member","team"})
    List<MemberTeam> findWithTeamAndMemberByOwner(Member owner);


    //By Team
    List<MemberTeam> findByTeam(Team team);
    @EntityGraph(attributePaths = "member")
    List<MemberTeam> findWithMemberByTeam(Team team);
    @EntityGraph(attributePaths = "owner")
    List<MemberTeam> findWithOwnerByTeam(Team team);


    //By Member
    @EntityGraph(attributePaths = {"owner","team"})
    List<MemberTeam> findWithOwnerAndTeamByMember(Member member);

}
