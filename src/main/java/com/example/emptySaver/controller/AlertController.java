package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.AlertDto;
import com.example.emptySaver.domain.dto.FriendDto;
import com.example.emptySaver.domain.dto.GroupDto;
import com.example.emptySaver.service.FriendService;
import com.example.emptySaver.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/notification")
public class AlertController {
    private final FriendService friendService;
    private final GroupService groupService;
    @GetMapping("/getAllList")
    public ResponseEntity<AlertDto> getAllNotifications(){
        List<FriendDto.FriendInfo> friendInfoList = friendService.getReceivedList();
        List<GroupDto.SimpleGroupRes> groupReceiveList = groupService.getGroupRequests("group");
        List<List<GroupDto.InviteInfo>> myOwnGroupReceiveList= new ArrayList<>();
        groupService.getMyOwnGroupId().forEach( id->
            myOwnGroupReceiveList.add(groupService.getInviteMemberList(id,"member")));
        AlertDto res = new AlertDto(friendInfoList, groupReceiveList, myOwnGroupReceiveList);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
