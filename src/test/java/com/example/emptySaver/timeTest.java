package com.example.emptySaver;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class timeTest {
    @Test
    void zoneTime(){
        System.out.println(ZonedDateTime.now());
        System.out.println(ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
        System.out.println(ZonedDateTime.now(ZoneId.of("America/Chicago")));
        System.out.println(LocalDateTime.now());
    }
}
