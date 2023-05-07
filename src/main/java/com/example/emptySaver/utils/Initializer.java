package com.example.emptySaver.utils;

import com.example.emptySaver.service.SubjectService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Scope(value = "prototype")
@Slf4j
public class Initializer implements CommandLineRunner {
    private final SubjectService subjectService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Server init Complete!!");
        subjectService.saveAllSubjectByYearAndTerm("2023", "A10");
        log.info("subject Save Complete");
    }
}
