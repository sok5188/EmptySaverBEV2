package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, Long> {
}
