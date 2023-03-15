package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member_Interest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemberInterestRepository extends MongoRepository<Member_Interest,Long> {
}
