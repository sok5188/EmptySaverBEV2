package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.FriendDto;
import com.example.emptySaver.domain.dto.TimeTableDto;
import com.example.emptySaver.service.FriendService;
import com.example.emptySaver.service.TimeTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/friend")
@Slf4j
public class FriendController {
    private final FriendService friendService;
    private final TimeTableService timeTableService;

/*
    @GetMapping("/getMatchingTimeFriendList")
    @Operation(summary = "친구 리스트에서 같이 할수 있는 친구 목록 반환")
    public ResponseEntity<FriendDto.res> getMatchingTimeFriendList(){
        List<FriendDto.FriendInfo> friendList = friendService.getFriendList();
        return new ResponseEntity<>(new FriendDto.res<>(friendList), HttpStatus.OK);
    }*/

    @PostMapping("/getFriendTimeTable")
    @Operation(summary = "친구 시간표 조회하기", description = "회원의 친구의 시간표를 조회하는 API")
    @Parameter(
            name = "groupMemberId",
            description = "대상이 되는 친구의 ID를 넘김"
    )
    public ResponseEntity<TimeTableDto.TimeTableInfo> getFriendTimeTable(final @RequestParam Long friendMemberId ,@RequestBody TimeTableDto.TimeTableRequestForm requestForm){
        //Long currentMemberId = memberService.getCurrentMemberId();
        log.info("build: " + requestForm.toString());
        TimeTableDto.TimeTableInfo timeTableInfo
                = timeTableService.getMemberTimeTableByDayNum(friendMemberId, requestForm.getStartDate(), requestForm.getEndDate(), false);
        return new ResponseEntity<>(timeTableInfo, HttpStatus.OK);
    }

    @GetMapping("/getList")
    @Operation(summary = "친구목록", description = "회원의 친구 목록을 조회하는 API")
    public ResponseEntity<FriendDto.res> getFriend(){
        List<FriendDto.FriendInfo> friendList = friendService.getFriendList();
        return new ResponseEntity<>(new FriendDto.res<>(friendList), HttpStatus.OK);
    }
    @GetMapping("/receiveList")
    @Operation(summary = "받은 친구 요청 목록 조회", description = "회원이 받은 친구 요청 목록을 조회하는 API")
    public ResponseEntity<FriendDto.res> getRequestedFriends(){
        List<FriendDto.FriendInfo> friendList = friendService.getReceivedList();
        return new ResponseEntity<>(new FriendDto.res<>(friendList), HttpStatus.OK);
    }

    @GetMapping("/requestList")
    @Operation(summary = "보낸 친구 요청 목록 조회", description = "회원이 보낸 친구 요청 목록을 조회하는 API")
    public ResponseEntity<FriendDto.res> getMyFriendRequests(){
        List<FriendDto.FriendInfo> friendList = friendService.getMySendList();
        return new ResponseEntity<>(new FriendDto.res<>(friendList), HttpStatus.OK);
    }
    @PostMapping("/request/{friendEmail}")
    @Operation(summary = "친구 요청 전송", description = "회원이 다른 회원의 이메일로 친구 요청을 전송하는 API")
    public ResponseEntity<String> sendFriendRequest(@PathVariable String friendEmail){
        log.info("got email: {} ",friendEmail);
        friendService.requestFriend(friendEmail);
        return new ResponseEntity<>("send friend request",HttpStatus.OK);
    }
    @PostMapping("/add/{friendId}")
    @Operation(summary = "친구 요청 승인", description = "친구 목록, 요청 목록같은 list들에서 같이 리턴된 friendId값 (!! friendMemberId아님 !!)을 이용해 해당 친구 관계를 승인하는 API")
    public ResponseEntity<String> addFriend(@PathVariable Long friendId){
        friendService.approveFriend(friendId);
        return new ResponseEntity<>("approve friend request",HttpStatus.OK);
    }
    @DeleteMapping("/deny/{friendId}")
    @Operation(summary = "친구 요청 거절", description = "친구 목록, 요청 목록같은 list들에서 같이 리턴된 friendId값 (!! friendMemberId아님 !!)을 이용해 받은 친구 요청을 거절하는 API")
    public ResponseEntity<String> denyFriendRequest(@PathVariable Long friendId){
        friendService.removeFriend(friendId,false);
        return new ResponseEntity<>("deny friend request",HttpStatus.OK);
    }
    @DeleteMapping("/delete/{friendId}")
    @Operation(summary = "친구 삭제", description = "친구 목록, 요청 목록같은 list들에서 같이 리턴된 friendId값 (!! friendMemberId아님 !!)을 이용해 친구관계를 삭제하는 API (## 추가로 만약 친구 요청을 취소하고 싶으면 여기로 보내면 됨)")
    public ResponseEntity<String> deleteFriend(@PathVariable Long friendId){
        friendService.removeFriend(friendId,true);
        return new ResponseEntity<>("delete friend request",HttpStatus.OK);
    }

}
