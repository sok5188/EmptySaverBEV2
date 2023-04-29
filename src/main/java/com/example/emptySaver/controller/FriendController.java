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
    @GetMapping("/get")
    @Operation(summary = "진구목록", description = "회원의 친구 목록을 조회하는 API")
    public ResponseEntity<FriendDto.res> getFriend(){
        List<FriendDto.FriendInfo> friendList = friendService.getFriendList();
        return new ResponseEntity<>(new FriendDto.res<>(friendList), HttpStatus.OK);
    }
    //TODO: 추가,삭제 기능 구현
//    @PostMapping("/add")
//    @Operation(summary = "친구추가", description = "친구 신청을 유저가 승인하는 경우 해당 친구 id를 인자로 해 유저의 친구목록에 추가하는 API")
//    public ResponseEntity<String> addFriend(@RequestParam("friendId") Long friendId){
//
//    }
//    @DeleteMapping("/delete")
//    @Operation(summary = "친구삭제", description = "해당 친구를 목록에서 삭제하는 API")
//    public ResponseEntity<String> deleteFriend(@RequestParam("friendId") Long friendId){
//
//    }
}
