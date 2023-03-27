package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Friend;
import com.example.emptySaver.domain.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class FriendRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private FriendRepository repository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void 친구추가(){
        Member member=new Member();
        member.setUsername("testUser");
        em.persist(member);

        Member member2=new Member();
        member2.setUsername("testUser22");
        em.persist(member2);

        Friend friend=new Friend();
        friend.addFriend(member,member2);
        em.persist(friend);

        Friend friend2=new Friend();
        friend2.addFriend(member2,member);
        em.persist(friend2);

        em.flush();
        em.clear();

        List<Friend> byOwner = repository.findWithFriendByOwner(member);
        List<Friend> byOwner2 = repository.findWithFriendByOwner(member2);

        assertThat(byOwner.size()).isEqualTo(1);
        assertThat(byOwner.get(0).getFriendMember().getUsername()).isEqualTo("testUser22");

        assertThat(byOwner2.size()).isEqualTo(1);
        assertThat(byOwner2.get(0).getFriendMember().getUsername()).isEqualTo("testUser");
    }
    @Test
    public void 친구삭제(){
        Member member=new Member();
        member.setUsername("testUser");
        em.persist(member);

        Member member2=new Member();
        member2.setUsername("testUser22");
        em.persist(member2);

        Friend friend=new Friend();
        friend.addFriend(member,member2);
        em.persist(friend);

        em.flush();
        em.clear();

        List<Friend> byOwner = repository.findWithFriendByOwner(member);

        memberRepository.delete(member2);
        List<Friend> byOwner2 = repository.findWithFriendByOwner(member);
        assertThat(byOwner2.size()).isEqualTo(0);
    }
    @Test
    public void 친구정보수정(){
        Member member=new Member();
        member.setUsername("testUser");
        em.persist(member);

        Member member2=new Member();
        member2.setUsername("testUser22");
        em.persist(member2);

        Friend friend=new Friend();
        friend.addFriend(member,member2);
        em.persist(friend);

        List<Friend> byOwner = repository.findWithFriendByOwner(member);
        String beforeName = byOwner.get(0).getFriendMember().getUsername();

        member2.setUsername("newTestUserName");

        String afterName = byOwner.get(0).getFriendMember().getUsername();

        assertThat(beforeName).isNotEqualTo(afterName);
    }
}