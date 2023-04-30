package com.example.emptySaver.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@ToString
public class Department {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String deptDiv; //by colg
    private String dept;    //by up_dept
    private String subDiv;  //by dept
}
