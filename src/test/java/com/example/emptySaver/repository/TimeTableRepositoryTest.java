package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Time_Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class TimeTableRepositoryTest {
    @Autowired
    private TimeTableRepository repository;

    @BeforeEach
    void beforeEach(){
        repository.deleteAll();
    }

    @DisplayName("timeTable save test")
    @Test
    void saveTimeTable(){
        Time_Table table = Time_Table.builder().title("noSoEasy").build();
        Time_Table savedTable = repository.save(table);

        assertThat(savedTable.getTitle()).isEqualTo(savedTable.getTitle());
    }
}