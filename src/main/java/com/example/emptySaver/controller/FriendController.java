package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.FriendDto;
import com.example.emptySaver.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
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
    @GetMapping("/getList")
    @Operation(summary = "진구목록", description = "회원의 친구 목록을 조회하는 API")
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
        //TODO: 친구 요청을 보내면 해당 회원에겐 알림이 가야 한다.
        log.info("got email: {} ",friendEmail);
        friendService.requestFriend(friendEmail);
        return new ResponseEntity<>("send friend request",HttpStatus.OK);
    }
    @PostMapping("/add/{friendEmail}")
    @Operation(summary = "친구 요청 승인", description = "친구 신청을 유저가 승인하는 경우 해당 친구 id를 인자로 해 유저의 친구목록에 추가하는 API")
    public ResponseEntity<String> addFriend(@PathVariable Long friendId){
        friendService.approveFriend(friendId);
        return new ResponseEntity<>("approve friend request",HttpStatus.OK);
    }
    @DeleteMapping("/deny/{friendEmail}")
    @Operation(summary = "친구 요청 거절", description = "받은 친구 요청을 거절하는 API")
    public ResponseEntity<String> denyFriendRequest(@PathVariable Long friendId){
        friendService.removeFriend(friendId,false);
        return new ResponseEntity<>("deny friend request",HttpStatus.OK);
    }
    @DeleteMapping("/delete/{friendEmail}")
    @Operation(summary = "친구 삭제", description = "친구를 삭제하는 API")
    public ResponseEntity<String> deleteFriend(@PathVariable Long friendId){
        friendService.removeFriend(friendId,true);
        return new ResponseEntity<>("delete friend request",HttpStatus.OK);
    }

}
