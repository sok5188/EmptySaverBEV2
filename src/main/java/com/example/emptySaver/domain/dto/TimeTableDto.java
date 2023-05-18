package com.example.emptySaver.domain.dto;

import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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

        //@Builder.Default
        private Boolean groupType = false;
        //@Builder.Default
        private Long groupId = -1l; //개인 스케줄은 -1
        private String groupName = "";
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

        //팀을 위한 데이터. 개인 스케줄은 기본 값으로 들어감
        @Builder.Default
        private Boolean groupType = false;
        @Builder.Default
        private Long groupId = -1l; //개인 스케줄은 -1
        @Builder.Default
        private String groupName = "notGroup";
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
    @Schema(description = "스케줄을 찾기위한 형식")
    public static class ScheduleSearchRequestForm{
        @Schema(description = "찾는 시작 시점, yyyy-mm-dd'T'hh:mm:ss 형식으로 보내기")
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startTime;    //only for Non_Periodic

        @Schema(description = "찾는 끝 시점, yyyy-mm-dd'T'hh:mm:ss 형식으로 보내기")
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime endTime;      //only for Non_Periodic
    }

    //유효성 검사
    static public void checkSchedulePostDtoValid(final SchedulePostDto dto){
        if(dto.getEndTime().isBefore(dto.getStartTime()))
            throw new BaseException(BaseResponseStatus.LOCAL_DATE_TIME_END_ERROR);
    }
}
