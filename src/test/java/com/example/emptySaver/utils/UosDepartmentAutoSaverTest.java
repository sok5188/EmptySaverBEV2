package com.example.emptySaver.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UosDepartmentAutoSaverTest {
    @Autowired
    private UosDepartmentAutoSaver autoSaver;

    @Test
    void departApiTest(){
        autoSaver.saveAllUOSDepartment();
    }
}