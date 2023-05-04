package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.MemberTeam;
import com.example.emptySaver.domain.entity.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
@DataJpaTest
class MemberTeamRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private MemberTeamRepository repository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;

    @Test
    public void 그룹에멤버추가(){
        Member member=new Member();
        member.setUsername("testUser");
        em.persist(member);

        Member member2=new Member();
        member2.setUsername("testUser22");
        em.persist(member2);

        Team team=new Team();
        team.setName("testTeam");
        em.persist(team);

        MemberTeam mt=new MemberTeam();
        mt.initMemberTeam(member,team,member);
        em.persist(mt);

        MemberTeam mt2=new MemberTeam();
        mt2.initMemberTeam(member2,team,member);
        em.persist(mt2);

        em.flush();
        em.clear();

        List<MemberTeam> byTeam = repository.findByTeam(team);
        assertThat(byTeam.size()).isEqualTo(2);
    }

    @Test
    public void 그룹에서멤버삭제(){
        Member member=new Member();
        member.setUsername("testUser");
        em.persist(member);

        Member member2=new Member();
        member2.setUsername("testUser22");
        em.persist(member2);

        Team team=new Team();
        team.setName("testTeam");
        em.persist(team);

        MemberTeam mt=new MemberTeam();
        mt.initMemberTeam(member,team,member);
        em.persist(mt);

        MemberTeam mt2=new MemberTeam();
        mt2.initMemberTeam(member2,team,member);
        em.persist(mt2);

        em.flush();
        em.clear();

        repository.delete(mt2);
        List<MemberTeam> byTeam = repository.findByTeam(team);
        assertThat(byTeam.size()).isEqualTo(1);
    }
    @Nested
    @DisplayName("탈퇴 관련 테스트")
    class onDeleteTest{
        @Test
        public void 팀삭제(){
            Member member=new Member();
            member.setUsername("testUser");
            em.persist(member);

            Member member2=new Member();
            member2.setUsername("testUser22");
            em.persist(member2);

            Team team=new Team();
            team.setName("testTeam");
            em.persist(team);

            MemberTeam mt=new MemberTeam();
            mt.initMemberTeam(member,team,member);
            em.persist(mt);

            MemberTeam mt2=new MemberTeam();
            mt2.initMemberTeam(member2,team,member);
            em.persist(mt2);

            em.flush();
            em.clear();

            teamRepository.delete(team);
            Long count = repository.count();
            assertThat(count).isEqualTo(0L);
        }

        @Test
        public void 그룹장탈퇴(){
            Member member=new Member();
            member.setUsername("testUser");
            em.persist(member);

            Member member2=new Member();
            member2.setUsername("testUser22");
            em.persist(member2);

            Team team=new Team();
            team.setName("testTeam");
            em.persist(team);

            MemberTeam mt=new MemberTeam();
            mt.initMemberTeam(member,team,member);
            em.persist(mt);

            MemberTeam mt2=new MemberTeam();
            mt2.initMemberTeam(member2,team,member);
            em.persist(mt2);

            em.flush();
            em.clear();

            memberRepository.delete(member);

            Long count = repository.count();
            assertThat(count).isEqualTo(0L);
        }
        @Test
        public void 멤버탈퇴(){
            Member member=new Member();
            member.setUsername("testUser");
            em.persist(member);

            Member member2=new Member();
            member2.setUsername("testUser22");
            em.persist(member2);

            Team team=new Team();
            team.setName("testTeam");
            em.persist(team);

            MemberTeam mt=new MemberTeam();
            mt.initMemberTeam(member,team,member);
            em.persist(mt);

            MemberTeam mt2=new MemberTeam();
            mt2.initMemberTeam(member2,team,member);
            em.persist(mt2);

            em.flush();
            em.clear();

            memberRepository.delete(member2);
            Long count = repository.count();
            assertThat(count).isEqualTo(1);
        }
    }

    @Test
    public void 쿼리테스트(){
        Member member=new Member();
        member.setUsername("testUser");
        em.persist(member);
        Member member2=new Member();
        member2.setUsername("testUser22");
        em.persist(member2);

        Team team=new Team();
        team.setName("testTeam");
        em.persist(team);
        Team team2=new Team();
        team2.setName("testTeam");
        em.persist(team2);


        MemberTeam mt=new MemberTeam();
        mt.initMemberTeam(member,team,member);
        em.persist(mt);
        MemberTeam mt2=new MemberTeam();
        mt2.initMemberTeam(member2,team,member);
        em.persist(mt2);

        MemberTeam mt3=new MemberTeam();
        mt3.initMemberTeam(member,team2,member2);
        em.persist(mt3);
        MemberTeam mt4=new MemberTeam();
        mt4.initMemberTeam(member2,team2,member2);
        em.persist(mt4);


        em.flush();
        em.clear();
//        System.out.println("--------------------------------");
//        List<MemberTeam> byOwner = repository.findWithMemberByOwner(member);
//        System.out.println(byOwner.get(0).getMember());
//        System.out.println("--------------------------------");
//        em.clear();
//        List<MemberTeam> withTeamByOwner = repository.findWithTeamByOwner(member);
//        System.out.println("withTeamByOwner.get(0).getTeam() = " + withTeamByOwner.get(0).getTeam());
//        System.out.println("--------------------------------");
//        em.clear();
//
//        List<MemberTeam> withTeamAndMemberByOwner = repository.findWithTeamAndMemberByOwner(member);
//        System.out.println("withTeamAndMemberByOwner.get(0).getTeam() = " + withTeamAndMemberByOwner.get(0).getTeam());
//        System.out.println("withTeamAndMemberByOwner.get(0).getMember() = " + withTeamAndMemberByOwner.get(0).getMember());
//        System.out.println("--------------------------------");
//        em.clear();
//
//        List<MemberTeam> withMemberByTeam = repository.findWithMemberByTeam(team);
//        System.out.println("withMemberByTeam.get(0).getMember = " + withMemberByTeam.get(0).getMember());
//        System.out.println("--------------------------------");
//        em.clear();
//
//        List<MemberTeam> withOwnerByTeam = repository.findWithOwnerByTeam(team);
//        System.out.println("withOwnerByTeam.get().getOwner() = " + withOwnerByTeam.get(0).getOwner());
//        System.out.println("withOwnerByTeam.get().getOwner() = " + withOwnerByTeam.get(1).getOwner());
//        System.out.println("--------------------------------");
//        em.clear();
//
//        List<MemberTeam> withOwnerAndTeamByMember = repository.findWithOwnerAndTeamByMember(member);
//        System.out.println("withOwnerAndTeamByMember.get(0).getTeam() = " + withOwnerAndTeamByMember.get(0).getTeam());
//        System.out.println("withOwnerAndTeamByMember.get(0).getOwner() = " + withOwnerAndTeamByMember.get(0).getOwner());
//        System.out.println("--------------------------------");
//        em.clear();



    }

}