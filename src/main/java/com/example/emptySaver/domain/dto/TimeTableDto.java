package com.example.emptySaver.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

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
        private List<List<Boolean>> bitListsPerDay = new ArrayList<>();
        @Builder.Default
        private List<List<ScheduleDto>> scheduleListPerDays = new ArrayList<List<ScheduleDto>>();
    }

    @Data
    @Builder
    public static class ScheduleDto{
        private Long id;
        private String name;
        private String body;

        @Builder.Default
        private List<Boolean> timeData = new ArrayList<>();
        private String timeStringData;
    }

    @Data
    @Builder
    public static class TeamScheduleDto{
        private Long id;
        private String name;
        private String body;
        private boolean periodicType;
        private String timeData;
    }

    @Data
    @Builder
    public static class GroupTimeTableInfo{
        private Long groupId;
        @Builder.Default
        private TimeTableInfo timeTableInfo;
    }

    @Data
    @Builder
    public static class MemberAllTimeTableInfo{
        private TimeTableInfo memberTimeTable;
        @Builder.Default
        private List<GroupTimeTableInfo> groupTimeTableList= new ArrayList<>();
    }

    @Data
    @Builder
    public static class SearchedScheduleDto{
        private Long id;
        private String name;
        private String body;
        private boolean periodicType;

        private GroupDto.DetailGroupRes groupInfo;
        private String timeData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchedulePostDto{
        private String name;
        private String body;
        private String periodicType;

        @Builder.Default
        private List<String> periodicTimeStringList = new ArrayList<>(); //only for Periodic

        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startTime;    //only for Non_Periodic
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime endTime;      //only for Non_Periodic
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeTableRequestForm{
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleSearchRequestForm{
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startTime;    //only for Non_Periodic
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime endTime;      //only for Non_Periodic
    }


}
