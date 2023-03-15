package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemberRepository extends MongoRepository<Member, Long> {
}
