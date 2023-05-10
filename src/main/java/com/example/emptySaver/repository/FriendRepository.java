package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Friend;
import com.example.emptySaver.domain.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    @EntityGraph(attributePaths = "friendMember")
    List<Friend> findWithFriendMemberByOwner(Member owner);

    @EntityGraph(attributePaths = "owner")
    List<Friend> findWithOwnerByFriendMember(Member friendMember);

    Optional<Friend> findFirstByOwnerAndFriendMember(Member owner, Member friendMember);

}
