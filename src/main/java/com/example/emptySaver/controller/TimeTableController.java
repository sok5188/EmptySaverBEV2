package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.AuthDto;
import com.example.emptySaver.domain.dto.TimeTableDto;
import com.example.emptySaver.service.MemberService;
import com.example.emptySaver.service.TimeTableService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/timetable")
@RequiredArgsConstructor
@Slf4j
public class TimeTableController {
    private final TimeTableService timeTableService;
    private final MemberService memberService;

    @GetMapping("/getTimeTable")
    @Operation(summary = "getTimeTableData", description = "로그인 한 유저, 자신의 timeTable의 정보를 가져옴")
    public ResponseEntity<TimeTableDto.TimeTableInfo> getMemberTimeTable(@RequestBody LocalDate startDate,@RequestBody LocalDate endDate){
        Long currentMemberId = memberService.getCurrentMemberId();
        TimeTableDto.TimeTableInfo timeTableInfo = timeTableService.getMemberTimeTableByDayNum(currentMemberId, startDate, endDate);
        return new ResponseEntity<>(timeTableInfo, HttpStatus.OK);
    }

    @PostMapping("/saveSchedule")
    @Operation(summary = "saveSchedule in Memebr time table", description = "로그인 한 유저가 스케줄 정보를 timetable에 추가")
    public ResponseEntity<String> addMemberSchedule(@RequestBody TimeTableDto.SchedulePostDto schedulePostData){
        Long currentMemberId = memberService.getCurrentMemberId();
        timeTableService.saveScheduleInTimeTable(currentMemberId, schedulePostData);
        return new ResponseEntity<>("Schedule saved for member", HttpStatus.OK);
    }

    @PostMapping("/updateSchedule")
    @Operation(summary = "update Schedule in Memebr time table", description = "특정 스케줄을 id로 변경")
    public ResponseEntity<String> updateSchedule(@RequestBody TimeTableDto.SchedulePostDto updateData){
        Long currentMemberId = memberService.getCurrentMemberId();
        //timeTableService.updateScheduleInTimeTable(currentMemberId, updateData);
        return new ResponseEntity<>("Schedule update for member", HttpStatus.OK);
    }

    @PostMapping("/deleteSchedule")
    @Operation(summary = "saveSchedule in Memebr time table", description = "특정 스케줄을 id로 지움, 따라서 id는 절대 변경 안되도록")
    public ResponseEntity<String> deleteSchedule(final @RequestBody Long scheduleId){
        timeTableService.deleteScheduleInTimeTable(scheduleId);
        return new ResponseEntity<>("Schedule deleted, id: " + scheduleId, HttpStatus.OK);
    }


}
