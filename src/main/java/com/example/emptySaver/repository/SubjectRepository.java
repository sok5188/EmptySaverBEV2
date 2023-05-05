package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findBySubjectnameContaining(String subjectname);
    List<Subject> findBySubjectname(String subjectname);
    List<Subject> findByDept(String findByDept);
    List<Subject> findByYearsAndTerm(String years, String term );
    boolean existsByYearsAndTerm(String years, String term );
}
