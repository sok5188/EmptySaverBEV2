package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Interest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InterestRepository extends MongoRepository<Interest,Long> {
}
