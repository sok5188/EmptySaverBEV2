package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.GroupDto;
import com.example.emptySaver.service.GroupService;
import com.example.emptySaver.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
@Slf4j
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final MemberService memberService;

    @PostMapping("/make")
    @Operation(summary = "그룹 추가", description = "그룹을 추가하는 API")
    public ResponseEntity<String> addGroup(@RequestBody GroupDto.GroupInfo groupDto){
        groupService.addGroup(groupDto);
        return new ResponseEntity<>("Make Group",HttpStatus.OK);
    }
    @DeleteMapping("/delete/{groupId}")
    @Operation(summary = "그룹 삭제", description = "그룹을 삭제하는 API")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId){
        groupService.deleteGroup(groupId);
        return new ResponseEntity<>("Delete Group",HttpStatus.OK);
    }
    @PutMapping("/addMember")
    @Operation(summary = "멤버를 그룹에 추가", description = "회원을 특정 그룹에 추가하는 API")
    public ResponseEntity<String> addMemberToTeam(@RequestBody GroupDto.memberGroupReq req){
        String memberName = groupService.addMemberToTeam(req.getMemberId(), req.getGroupId());
        return new ResponseEntity<>("add Member "+memberName +" to Group",HttpStatus.OK);
    }
    @DeleteMapping("/deleteMember")
    @Operation(summary = "멤버를 그룹에서 삭제", description = "회원을 그룹에서 삭제하는 API")
    public ResponseEntity<String> deleteMember(@RequestBody GroupDto.memberGroupReq req){
        groupService.deleteMemberFromGroup(req.getMemberId(), req.getGroupId());
        return new ResponseEntity<>("delete Member "+req.getMemberId(),HttpStatus.OK);
    }
    @DeleteMapping("/deleteMe/{groupId}")
    @Operation(summary = "자신을 그룹에서 삭제", description = "자신이 그룹을 탈퇴하는 API")
    public ResponseEntity<String> deleteMember(@PathVariable Long groupId){
        Long currentMemberId = memberService.getCurrentMemberId();
        groupService.deleteMemberFromGroup(currentMemberId,groupId);
        return new ResponseEntity<>("delete Me",HttpStatus.OK);
    }


    //--------------
    //TODO: 아래 API 구현

    @GetMapping("/getCategoryTeam/{label}")
    @Operation(summary = "해당 카테고리의 팀 찾기", description = "특정 카테고리(상위)의 그룹 목록을 반환하는 API")
    public ResponseEntity<GroupDto.res> getCategoryTeam(@PathVariable String label){
        GroupDto.res res=new GroupDto.res(groupService.getGroupByType(label));
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
    @GetMapping("/getAllGroup")
    @Operation(summary = "전체 그룹 조회", description = "서비스 내 모든 그룹 리스트 반환하는 API")
    public ResponseEntity<GroupDto.res> getAllGroup(){
        List<GroupDto.SimpleGroupRes> allGroup = groupService.getAllGroup();
        GroupDto.res res=new GroupDto.res<>(allGroup);
        return new ResponseEntity<>(res,HttpStatus.OK);
    }
    @GetMapping("/getMyGroup")
    @Operation(summary = "자신의 그룹 조회",description = "자신이 속한 그룹 리스트를 반환하는 API")
    public ResponseEntity<GroupDto.res> getMyGroup(){
        GroupDto.res res=new GroupDto.res<>(groupService.getMyGroup());
        return new ResponseEntity<>(res,HttpStatus.OK);
    }
    @GetMapping("/getGroupMember/{groupId}")
    @Operation(summary = "그룹 구성원 목록 조회",description = "그룹 내 구성원들의 정보를 조회하는 API")
    public ResponseEntity<GroupDto.GroupMemberRes> getGroupMember(@PathVariable Long groupId){
        return new ResponseEntity<>(groupService.getGroupMembers(groupId),HttpStatus.OK);
    }
    @GetMapping("/getGroupDetail/{groupId}")
    @Operation(summary = "그룹 상세 조회 페이지",description = "그룹의 상세정보를 조회하는 API")
    public ResponseEntity<GroupDto.DetailGroupRes> getGroupDetail(@PathVariable Long groupId){
        return new ResponseEntity<>(groupService.getGroupDetails(groupId),HttpStatus.OK);
    }






    //TODO : 그룹 일정 생성 API, 그룹 일정 수정 API (미래 일정만 가능하게), 그룹 일정 삭제 API
    // 공지사항 부분?
    // 일정 생성 시 알림 전송 기능 필요
}
