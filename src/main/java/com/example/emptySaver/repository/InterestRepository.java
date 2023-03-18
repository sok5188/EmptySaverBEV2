package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interest,Long> {
}
