package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Friend;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FriendRepository extends MongoRepository<Friend, Long> {
}
