package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupRepository extends MongoRepository<Group,Long> {
}
