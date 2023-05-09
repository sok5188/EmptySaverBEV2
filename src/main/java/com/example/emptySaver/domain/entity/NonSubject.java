package com.example.emptySaver.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NonSubject {
    @Id
    @GeneratedValue
    @Column(name = "non_subject_id")
    private Long id;
    private String courseName;
    private String applyDate;
    private String runDate;
    private String targetDepartment;
    private String targetGrade;
    private String url;
}
