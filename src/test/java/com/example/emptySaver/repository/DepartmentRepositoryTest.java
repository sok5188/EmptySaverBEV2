package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;


//@SpringBootTest
//@ActiveProfiles("test")
@DataJpaTest
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void beforeEach(){
        departmentRepository.deleteAll();
    }
/*
    @Test
    void saveDepartment(){
        //given
        Department department = Department.builder().dept("A1014").subDiv("A10001").build();

        //when
        Department savedDepartment = departmentRepository.save(department);

        //then
        assertThat(savedDepartment.getDept()).isEqualTo(department.getDept());

    }*/
}