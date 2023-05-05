package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.TimeTableDto;
import com.example.emptySaver.service.MemberService;
import com.example.emptySaver.service.TimeTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/timetable")
@RequiredArgsConstructor
@Slf4j
public class TimeTableController {
    private final TimeTableService timeTableService;
    private final MemberService memberService;

    @PostMapping("/getTimeTable")
    @Operation(summary = "TimeTable 정보 가져오기", description = "로그인 한 유저, 자신의 timeTable의 정보를 가져옴")
    @Parameter(
            name = "requestForm",
            description = "startDate와 endDate는 날짜 정보만을 필요로 한다.\n" +
                    " 따라서 'yyyy-MM-dd' 형식으로 String에 담아 보내면 자동으로 형변환이 진행됨.\n" +
                    " 형식에 맞추지 않으면 오류"

    )
    public ResponseEntity<TimeTableDto.TimeTableInfo> getMemberTimeTable(@RequestBody TimeTableDto.TimeTableRequestForm requestForm){
        Long currentMemberId = memberService.getCurrentMemberId();
        log.info("build: " + requestForm.toString());
        TimeTableDto.TimeTableInfo timeTableInfo
                = timeTableService.getMemberTimeTableByDayNum(currentMemberId, requestForm.getStartDate(), requestForm.getEndDate());
        return new ResponseEntity<>(timeTableInfo, HttpStatus.OK);
    }

    @PostMapping("/saveSchedule")
    @Operation(summary = "timetable에 스케줄 저장", description = "로그인 한 유저가 스케줄 정보를 timetable에 추가")
    @Parameter(
            name = "SchedulePostDto",
            description = "가장 중요한 부분은 periodicType설정 임다. \n" +
                    "true로 하면 주기적 데이터로 인식하여 long[] timeBitData를 반드식 넣어줘여하고\n" +
                    "periodicType = false로 하면 비주기적 데이터로 인식하여 startTime과 endTime이 필요합니다. \n" +
                    "startTime과 endTime은 'yyyy-MM-dd'T'HH:mm:ss'형식의 String으로 보내면 인식됩니다."

    )
    public ResponseEntity<String> addMemberSchedule(@RequestBody TimeTableDto.SchedulePostDto schedulePostData){
        Long currentMemberId = memberService.getCurrentMemberId();
        log.info("build: " + schedulePostData.toString());
        timeTableService.saveScheduleInTimeTable(currentMemberId, schedulePostData);
        return new ResponseEntity<>("Schedule saved for member", HttpStatus.OK);
    }

    @PostMapping("/updateSchedule")
    @Operation(summary = "특정 스케줄 변경", description = "특정 스케줄을 scheduleId를 이용해서 변경")
    @Parameter(
            name = "scheduleId",
            description = "body가 아닌 uri에 담아서 보내기." +
                    "반드시 db에 존재하는 scheduleId 이어야 하므로,\n " +
                    "getMemberTimeTable로 얻은 정보에서 scheduleId 추출해 사용."
    )
    public ResponseEntity<String> updateSchedule(final @RequestParam Long scheduleId, final @RequestBody TimeTableDto.SchedulePostDto updateData){
        timeTableService.updateScheduleInTimeTable(scheduleId, updateData);
        return new ResponseEntity<>("Schedule update, id: " +scheduleId, HttpStatus.OK);
    }

    @PostMapping("/deleteSchedule")
    @Operation(summary = "특정 스케줄 삭제", description = "특정 스케줄을 id로 지움, 따라서 id는 절대 변경 안되도록")
    @Parameter(
            name = "scheduleId",
            description = "body가 아닌 uri에 담아서 보내기." +
                    "반드시 db에 존재하는 scheduleId 이어야 하므로,\n " +
                    "getMemberTimeTable로 얻은 정보에서 scheduleId 추출해 사용."
    )
    public ResponseEntity<String> deleteSchedule(final @RequestParam Long scheduleId){
        timeTableService.deleteScheduleInTimeTable(scheduleId);
        return new ResponseEntity<>("Schedule deleted, id: " + scheduleId, HttpStatus.OK);
    }

    //찬민이형이 말한 형식대로 전달
    //LocalTimeData 변환
    //Group의 TimeTable 접근

}
