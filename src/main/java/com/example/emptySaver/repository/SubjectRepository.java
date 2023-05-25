package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findBySubjectnameContaining(String subjectname);
    List<Subject> findBySubjectname(String subjectname);
    List<Subject> findByDept(String dept);
    List<Subject> findByShyrAndDeptContaining(String shyr, String dept);
    boolean existsByYearsAndTerm(String years, String term );

    @Query(value = "select distinct s.upper_div_name from subject s",nativeQuery = true)
    List<String> findDistinctUpperDivName();
}
