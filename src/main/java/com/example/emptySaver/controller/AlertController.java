package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.AlertDto;
import com.example.emptySaver.domain.dto.FriendDto;
import com.example.emptySaver.domain.dto.GroupDto;
import com.example.emptySaver.service.AlertService;
import com.example.emptySaver.service.FriendService;
import com.example.emptySaver.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/notification")
public class AlertController {
        private final AlertService alertService;

//    @GetMapping("/getAll")
//    public ResponseEntity<AlertDto> getAllNotifications(){
//        List<FriendDto.FriendInfo> friendInfoList = friendService.getReceivedList();
//        List<GroupDto.SimpleGroupRes> groupReceiveList = groupService.getGroupRequests("group");
//        List<List<GroupDto.InviteInfo>> myOwnGroupReceiveList= new ArrayList<>();
//        groupService.getMyOwnGroupId().forEach( id->
//            myOwnGroupReceiveList.add(groupService.getInviteMemberList(id,"member")));
//        AlertDto res = new AlertDto(friendInfoList, groupReceiveList, myOwnGroupReceiveList);
//        return new ResponseEntity<>(res, HttpStatus.OK);
//    }

    @PutMapping("/check")
    @Operation(summary = "확인한 알림을 기록하는 API", description = "알림목록의 알림id를 통해 읽은(클릭한)알림을 기록하기 위한 API")
    public ResponseEntity<String> checkNotification(@RequestBody Long notificationId){
        alertService.checkRead(notificationId);
        return new ResponseEntity<>("Saved Notification",HttpStatus.OK);
    }

    @GetMapping("/getAll")
    @Operation(summary = "받은 알림 목록 조회")
    //원래는 이렇게 DTO리스트를 반환하면 "더"티" 코드
    public ResponseEntity<List<AlertDto>> getAllNoti(){
        return new ResponseEntity<>(alertService.getAllNotification(), HttpStatus.OK);
    }

}
