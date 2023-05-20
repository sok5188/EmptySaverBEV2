package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.MemberInterest;
import com.example.emptySaver.domain.entity.category.Play;
import com.example.emptySaver.domain.entity.category.PlayType;
import com.example.emptySaver.domain.entity.category.Movie;
import com.example.emptySaver.domain.entity.category.MovieType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
@DataJpaTest
class MemberInterestRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private MemberInterestRepository repository;

    @Test
    public void saveTest(){
        Member member=new Member();
        member.setUsername("testUser");
        em.persist(member);

        Member member2=new Member();
        member2.setUsername("testUser2222");
        em.persist(member2);

        Movie movie=new Movie();
        movie.setName("바람");
        movie.setMovieGenre(MovieType.ACTION);
        em.persist(movie);

        Play play =new Play();
        play.setPlayType(PlayType.ONLINE_GAME);
        play.setName("SuddenAttack");
        em.persist(play);

        MemberInterest memberInterest=new MemberInterest();
        memberInterest.setMember(member);
        memberInterest.setCategory(movie);
        em.persist(memberInterest);

        MemberInterest memberInterest2=new MemberInterest();
        memberInterest2.setMember(member);
        memberInterest2.setCategory(play);
        em.persist(memberInterest2);

        MemberInterest memberInterest3=new MemberInterest();
        memberInterest3.setMember(member2);
        memberInterest3.setCategory(movie);
        em.persist(memberInterest3);

        MemberInterest memberInterest4=new MemberInterest();
        memberInterest4.setMember(member2);
        memberInterest4.setCategory(play);
        em.persist(memberInterest4);

        em.flush();
        em.clear();

        List<MemberInterest> byMember = repository.findWithCategoryByMember(member2);
        List<MemberInterest> byCategory = repository.findWithMemberByCategory(movie);

        assertThat(byMember.size()).isEqualTo(2);
        assertThat(byCategory.size()).isEqualTo(2);
        assertThat(byCategory.get(0).getMember().getUsername()).isEqualTo(member.getUsername());
        assertThat(byMember.get(0).getCategory().getClass()).isEqualTo(movie.getClass());
    }
}