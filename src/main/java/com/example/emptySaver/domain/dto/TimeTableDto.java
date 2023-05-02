package com.example.emptySaver.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
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
}
