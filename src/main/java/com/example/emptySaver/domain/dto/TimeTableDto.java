package com.example.emptySaver.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class TimeTableDto {
    @Data
    @Builder
    public static class TimeTableInfo{
        private LocalDate startDate;
        private LocalDate endData;
        @Builder.Default
        private List<Long> bitDataPerDays  = new ArrayList<>();
        @Builder.Default
        private List<List<ScheduleDto>> scheduleListPerDays = new ArrayList<List<ScheduleDto>>();
    }

    @Data
    @Builder
    public static class ScheduleDto{
        private String name;
        private Long timeBitData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchedulePostDto{
        private String name;
        private String body;
        private boolean isPeriodic;

        private long[] timeBitData;           //only for Periodic

        private LocalDateTime startTime;    //only for Non_Periodic
        private LocalDateTime endTime;      //only for Non_Periodic
    }
}
