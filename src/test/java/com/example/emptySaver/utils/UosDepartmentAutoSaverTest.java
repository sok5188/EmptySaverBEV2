package com.example.emptySaver.utils;

import com.example.emptySaver.domain.entity.Department;
import com.example.emptySaver.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UosDepartmentAutoSaverTest {
    @Autowired
    private UosDepartmentAutoSaver autoSaver;
    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void departApiTest(){
        autoSaver.saveAllUOSDepartment();

        for (Department department : departmentRepository.findAll()) {
            System.out.println(department);
        }
    }
}