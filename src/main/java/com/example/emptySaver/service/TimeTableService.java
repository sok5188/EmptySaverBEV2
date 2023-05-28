package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.TimeTableDto;
import com.example.emptySaver.domain.entity.Schedule;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface TimeTableService {
    List<TimeTableDto.SearchedScheduleDto> getSearchedScheduleDtoList(TimeTableDto.ScheduleSearchRequestForm searchForm);

    List<TimeTableDto.SearchedScheduleDto> convertScheduleListToSearchedScheduleDtoList(List<Schedule> scheduleList);

    @Transactional
    void saveScheduleByTeam(Long teamId, final Long OwnerId,boolean isPublicTypeSchedule, TimeTableDto.SchedulePostDto schedulePostDto);

    List<TimeTableDto.TeamScheduleDto> getTeamScheduleList(Long teamId);

    void setCheckTeamSchedule(final Long scheduleId, final boolean accept);

    TimeTableDto.MemberAllTimeTableInfo getMemberAllTimeTableInfo(Long memberId, LocalDate startDate, LocalDate endDate);

    TimeTableDto.TimeTableInfo getMemberTimeTableByDayNum(Long memberId, LocalDate startDate, LocalDate endDate);

    //schedule id로 복사본 저장
    @Transactional
    void saveScheduleInDB(Long memberId, Long scheduleId);

    //멤버로 스케줄 저장
    @Transactional
    void saveScheduleInTimeTable(Long memberId, TimeTableDto.SchedulePostDto schedulePostData);

    //멤버로 수정
    @Transactional
    void updateScheduleInTimeTable(Long scheduleId, TimeTableDto.SchedulePostDto updatePostData);

    //팀 전체 수정
    void updateTeamSchedule(final Long teamId,final Long scheduleId, TimeTableDto.SchedulePostDto updatePostData);

    @Transactional
    void deleteScheduleInTimeTable(Long scheduleId);

    void deleteTeamSchedule(final Long teamId,final Long scheduleId);
}
