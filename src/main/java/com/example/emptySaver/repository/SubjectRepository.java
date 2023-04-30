package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
