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
    @Schema(description = "멤버의 시간표 정보")
    public static class TimeTableInfo{
        @Schema(description = "찾는 시작 시점, yyyy-mm-dd 형식으로 보내기")
        private LocalDate startDate;

        @Schema(description = "찾는 종료 시점, yyyy-mm-dd 형식으로 보내기")
        private LocalDate endData;
        @Builder.Default
        private List<List<Boolean>> bitListsPerDay = new ArrayList<>();
        @Builder.Default
        private List<List<ScheduleDto>> scheduleListPerDays = new ArrayList<List<ScheduleDto>>();
    }

    @Data
    @Builder
    @Schema(description = "스케줄 정보")
    public static class ScheduleDto{
        private Long id;
        private String name;
        private String body;

        @Builder.Default
        private List<Boolean> timeData = new ArrayList<>();
        private String timeStringData;

        //@Builder.Default
        @Schema(description = "개인 스케줄인 경우 false")
        @Builder.Default
        private Boolean groupType = false;
        //@Builder.Default

        @Builder.Default
        private boolean hideType = false;

        @Schema(description = "개인 스케줄인 경우 -1")
        @Builder.Default
        private Long groupId = -1l; //개인 스케줄은 -1
        @Schema(description = "개인 스케줄인 경우 notGroup")
        @Builder.Default
        private String groupName = "";
    }

    @Data
    @Builder
    @Schema(description = "그룹의 스케줄 간단 정보")
    public static class TeamScheduleDto{
        private Long id;
        private String name;
        private String body;
        private boolean periodicType;
        private String timeData;
        private boolean isRead;
    }

    @Data
    @Builder
    @Schema(description = "그룹의 timetable")
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
    @Schema(description = "검색된 스케줄을 반환하는 form")
    public static class SearchedScheduleDto{
        private Long id;
        private String name;
        private String body;
        private boolean periodicType;

        @Schema(description = "카테고리가 존재하는 경우 값이 들어있음")
        private String category;
        @Schema(description = "카테고리가 존재하는 경우 값이 들어있음")
        private String subCategory;

        @Schema(description = "스케줄을 만든 그룹의 내용")
        private GroupDto.DetailGroupRes groupInfo;
        private String timeData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "저장하려는 스케줄의 정보")
    public static class SchedulePostDto{
        private String name;
        private String body;

        @Schema(description = "카테고리가 존재하는 스케줄인 경우, 없으면 안보내도 됨")
        private String category;
        @Schema(description = "카테고리가 존재하는 스케줄인 경우, 없으면 안보내도 됨")
        private String subCategory;

        @Schema(description = "스케줄이 주기적인지의 정보를 문자열로")
        private String periodicType;

        @Schema(description = "스케줄을 타인에게 숨길지 여부. true라면 숨김. 빈값으로 넘기면 일단 공개시킴")
        @Builder.Default
        private Boolean hideType = false;

        @Builder.Default
        private List<String> periodicTimeStringList = new ArrayList<>(); //only for Periodic

        @Schema(description = "스케줄이 비주기적인 경우 yyyy-MM-dd'T'HH:mm:ss형식으로 보내기")
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startTime;    //only for Non_Periodic
        @Schema(description = "스케줄이 비주기적인 경우 yyyy-MM-dd'T'HH:mm:ss형식으로 보내기")
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime endTime;      //only for Non_Periodic

        //팀을 위한 데이터. 개인 스케줄은 기본 값으로 들어감
        @Schema(description = "스케줄이 그룹에서 만들어진 것인지에 대한 여부, 개인 스케줄이면 안보내도 됨")
        @Builder.Default
        private Boolean groupType = false;
        @Schema(description = "스케줄이 그룹에서 만들어진 경우 그룹의 id를 보내줌, 개인 스케줄이면 안보내도 됨")
        @Builder.Default
        private Long groupId = -1l; //개인 스케줄은 -1
        @Schema(description = "스케줄이 그룹에서 만들어진 경우 그룹의 이름 보내줌, 개인 스케줄이면 안보내도 됨")
        @Builder.Default
        private String groupName = "notGroup";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "search 대상의 시작과 끝의 정보를 날짜로만 받기 위한 형식")
    public static class TimeTableRequestForm{
        @Schema(description = "찾는 시작 시점, yyyy-mm-dd 형식으로 보내기")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @Schema(description = "찾는 끝 시점, yyyy-mm-dd 형식으로 보내기")
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
        if(dto.getPeriodicType().equals("true"))
            return;

        if(dto.getEndTime().isBefore(dto.getStartTime()))
            throw new BaseException(BaseResponseStatus.LOCAL_DATE_TIME_END_ERROR);
    }

    static public void checkTimeTableRequestFormValid(final TimeTableRequestForm form){
        if(form.getStartDate().isAfter(form.getEndDate()))
            throw new BaseException(BaseResponseStatus.LOCAL_DATE_TIME_END_ERROR);

        return;
    }

    static public void checkScheduleFormValid(final TimeTableDto.SchedulePostDto schedulePostDto){
        TimeTableDto.checkSchedulePostDtoValid(schedulePostDto);
        try{
            if(schedulePostDto.getPeriodicType().equals("true")){
                if(schedulePostDto.getPeriodicTimeStringList() == null){
                    throw new BaseException(BaseResponseStatus.NOT_AVAILABLE_SCHEDULE_FORM);
                }
            }else{
                if(schedulePostDto.getStartTime() == null)
                    throw new BaseException(BaseResponseStatus.NOT_AVAILABLE_SCHEDULE_FORM);
            }
        }catch (NullPointerException e){
            throw new BaseException(BaseResponseStatus.NOT_AVAILABLE_SCHEDULE_FORM);
        }
    }

    static public void checkLocalDateValid(final LocalDateTime startTime, final LocalDateTime endTime){
        if(endTime.isBefore(startTime))
            throw new BaseException(BaseResponseStatus.LOCAL_DATE_TIME_END_ERROR);
    }
}
