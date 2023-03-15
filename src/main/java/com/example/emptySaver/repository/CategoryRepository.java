package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, Long> {
}
