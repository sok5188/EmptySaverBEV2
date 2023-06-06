package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.GroupDto;
import com.example.emptySaver.domain.dto.TimeTableDto;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.service.GroupService;
import com.example.emptySaver.service.MemberService;
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
@RequestMapping("/group")
@Slf4j
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final MemberService memberService;
    private final TimeTableService timeTableService;

    @PostMapping("/make")
    @Operation(summary = "그룹 추가", description = "!!(categoryName:영어 / labelName: 한글)그룹을 추가하는 API ")
    public ResponseEntity<String> addGroup(@RequestBody GroupDto.GroupInfo groupDto){
        groupService.addGroup(groupDto);
        return new ResponseEntity<>("Make Group",HttpStatus.OK);
    }
    @DeleteMapping("/delete/{groupId}")
    @Operation(summary = "그룹 삭제", description = "그룹을 삭제하는 API")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId){
        if(!groupService.checkOwner(groupId))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        if(!groupService.checkAlone(groupId))
            throw new BaseException(BaseResponseStatus.NOT_ALONE_ERROR);
        groupService.deleteGroup(groupId);
        return new ResponseEntity<>("Delete Group",HttpStatus.OK);
    }
    @PutMapping("/addMember")
    @Operation(summary = "멤버의 가입신청을 수락하는 API", description = "그룹장이 받은 멤버의 가입신청을 수락하는 API")
    public ResponseEntity<String> addMemberToTeam(@RequestBody GroupDto.memberGroupReq req){
        if(!groupService.checkOwner(req.getGroupId()))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        String memberName = groupService.acceptMember(req.getMemberId(), req.getGroupId());
        return new ResponseEntity<>("add Member "+memberName +" to Group",HttpStatus.OK);
    }
    @PutMapping("/acceptInvite/{groupId}")
    @Operation(summary = "회원이 초대를 수락하는 API", description = "멤버가 받은 초대를 수락하여 그룹에 실제로 추가되는 API")
    public ResponseEntity<String> addMemberToTeam(@PathVariable Long groupId){
        String memberName = groupService.acceptMember(memberService.getCurrentMemberId(), groupId);
        return new ResponseEntity<>("add Member "+memberName +" to Group",HttpStatus.OK);
    }
    @DeleteMapping("/deleteMember")
    @Operation(summary = "멤버를 그룹에서 삭제 + 보낸 초대를 취소", description = "회원을 그룹에서 삭제하거나 보낸 초대를 삭제하는 API")
    public ResponseEntity<String> deleteMember(@RequestBody GroupDto.memberGroupReq req){
        if(!groupService.checkOwner(req.getGroupId()))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        groupService.deleteMemberFromGroup(req.getMemberId(), req.getGroupId());
        return new ResponseEntity<>("delete Member "+req.getMemberId(),HttpStatus.OK);
    }
    @DeleteMapping("/deleteMe/{groupId}")
    @Operation(summary = "자신을 그룹에서 삭제 + 초대 거절", description = "속한 그룹에서 자신이 탈퇴하거나 받은 초대를 거절하는 API")
    public ResponseEntity<String> deleteMember(@PathVariable Long groupId){
        if(groupService.checkOwner(groupId)&&!groupService.checkAlone(groupId))
            throw new BaseException(BaseResponseStatus.NOT_ALONE_ERROR);
        Long currentMemberId = memberService.getCurrentMemberId();
        groupService.deleteMemberFromGroup(currentMemberId,groupId);
        return new ResponseEntity<>("delete Me",HttpStatus.OK);
    }
    @GetMapping("/getLabelTeam")
    @Operation(summary = "카테고리 이름(영어) + 라벨(한글)의 팀 찾기", description = "카테고리(영어) + 라벨(한글) 조합으로 해당하는 그룹 목록을 반환하는 API(공개그룹만)")
    public ResponseEntity<List<GroupDto.SimpleGroupRes>> getLabelTeam(@RequestParam String categoryName, @RequestParam String label){
//        GroupDto.res res=new GroupDto.res(groupService.getGroupByType(categoryName,label);
        List<GroupDto.SimpleGroupRes> groupByType = groupService.getGroupByType(categoryName, label);
        return new ResponseEntity<>(groupByType, HttpStatus.OK);
    }
    @GetMapping("/getCategoryTeam/{categoryName}")
    @Operation(summary = "해당 카테고리의 팀 찾기", description = "특정 카테고리(상위)의 그룹 목록을 반환하는 API(공개그룹만)")
    public ResponseEntity<List<GroupDto.SimpleGroupRes>> getCategoryTeam(@PathVariable String categoryName){
//        GroupDto.res res=new GroupDto.res(groupService.getGroupByCategoryName(categoryName));
        List<GroupDto.SimpleGroupRes> groupByCategoryName = groupService.getGroupByCategoryName(categoryName);
        return new ResponseEntity<>(groupByCategoryName, HttpStatus.OK);
    }
    @GetMapping("/getAllGroup")
    @Operation(summary = "전체 그룹 조회", description = "서비스 내 모든 그룹 리스트 반환하는 API(공개그룹만)")
    public ResponseEntity<List<GroupDto.SimpleGroupRes>> getAllGroup(){
        List<GroupDto.SimpleGroupRes> allGroup = groupService.getAllGroup();
//        GroupDto.res res=new GroupDto.res<>(allGroup);
        return new ResponseEntity<>(allGroup,HttpStatus.OK);
    }
    @GetMapping("/getMyGroup")
    @Operation(summary = "자신의 그룹 조회",description = "자신이 !소속된! 그룹 리스트를 반환하는 API")
    public ResponseEntity<List<GroupDto.SimpleGroupRes>> getMyGroup(){
//        GroupDto.res res=new GroupDto.res<>(groupService.getMyGroup());
        List<GroupDto.SimpleGroupRes> myGroup = groupService.getMyGroup();
        return new ResponseEntity<>(myGroup,HttpStatus.OK);
    }

    @GetMapping("/getGroupMember/{groupId}")
    @Operation(summary = "그룹 구성원 목록 조회",description = "그룹 내 구성원들의 정보를 조회하는 API")
    public ResponseEntity<GroupDto.GroupMemberRes> getGroupMember(@PathVariable Long groupId){
        if(!groupService.checkBelong(groupId))
            throw new BaseException(BaseResponseStatus.NOT_BELONG_ERROR);
        return new ResponseEntity<>(groupService.getGroupMembers(groupId),HttpStatus.OK);
    }
    @GetMapping("/getGroupDetail/{groupId}")
    @Operation(summary = "그룹 상세 조회 페이지",description = "그룹의 상세정보를 조회하는 API(비공개 그룹은 그룹원만 조회 가능)")
    public ResponseEntity<GroupDto.DetailGroupRes> getGroupDetail(@PathVariable Long groupId){
        if(!groupService.checkBelong(groupId)&&!groupService.checkPublic(groupId))
            throw new BaseException(BaseResponseStatus.NOT_PUBLIC_ERROR);
        return new ResponseEntity<>(groupService.getGroupDetails(groupId),HttpStatus.OK);
    }

    @PostMapping("/sendInvite")
    @Operation(summary = "멤버를 그룹에 초대", description = "그룹에서 멤버를 초대하는 API(그룹장만 가능)")
    public ResponseEntity<String> sendInviteFromTeam(@RequestBody GroupDto.memberGroupReq req){
        if(!groupService.checkOwner(req.getGroupId()))
            return new ResponseEntity<>("그룹장만 초대를 보낼 수 있습니다.",HttpStatus.BAD_REQUEST);
        String member = groupService.addMemberToTeam(req.getMemberId(), req.getGroupId(),"group");
        return new ResponseEntity<>("invite Member "+member +" to Group",HttpStatus.OK);
    }

    //회원이 보낸 가입신청은 그룹장만 수정 가능하고
    //그룹이 보낸 초대는 받은 회원만 수락 가능하다

    @PostMapping("/sendRequest/{groupId}")
    @Operation(summary = "멤버가 그룹에 가입신청", description = "그룹에 들어가고 싶은 회원이 가입신청을 하는 API (해당 그룹에 가입되어 있지 않은 사용자만 가능)")
    public ResponseEntity<String> sendRequestFromMember(@PathVariable Long groupId){
        if(groupService.checkBelong(groupId))
            return new ResponseEntity<>("이미 가입되어 있습니다.",HttpStatus.BAD_REQUEST);
        if(!groupService.checkPublic(groupId))
            return new ResponseEntity<>("비공개 그룹입니다.",HttpStatus.BAD_REQUEST);
        Long currentMemberId = memberService.getCurrentMemberId();
        String s = groupService.addMemberToTeam(currentMemberId, groupId,"member");
        return new ResponseEntity<>("가입신청을 넣었습니다.",HttpStatus.OK);
    }

    @GetMapping("/getMemberRequestList")
    @Operation(summary = "멤버가 가입신청을 보낸 그룹 목록 조회", description = "회원이 가입신청을 보낸 그룹 리스트를 확인하는 API")
    public ResponseEntity<List<GroupDto.SimpleGroupRes>> getMemberRequestGroupList(){
//        GroupDto.res res = new GroupDto.res(groupService.getGroupRequests("member"));
        List<GroupDto.SimpleGroupRes> res = groupService.getGroupRequests("member");
        return new ResponseEntity<>(res,HttpStatus.OK);
    }
    @GetMapping("/getMemberReceiveList")
    @Operation(summary = "멤버가 받은 그룹 가입 권유 목록 조회", description = "회원이 가입신청을 받은 그룹 리스트를 확인하는 API")
    public ResponseEntity<List<GroupDto.SimpleGroupRes>> getMemberReceiveGroupList(){
//        GroupDto.res res = new GroupDto.res(groupService.getGroupRequests("group"));
        List<GroupDto.SimpleGroupRes> res = groupService.getGroupRequests("group");
        return new ResponseEntity<>(res,HttpStatus.OK);
    }

    @GetMapping("/getReceiveList/{groupId}")
    @Operation(summary = "그룹장이 가입신청 목록 조회", description = "회원들이 보낸 가입신청을 그룹장이 확인하는 API")
    public ResponseEntity<List<GroupDto.InviteInfo>> getReceiveGroupList(@PathVariable Long groupId){
        if(!groupService.checkOwner(groupId))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        if(!groupService.checkPublic(groupId))
            throw new BaseException(BaseResponseStatus.NOT_PUBLIC_ERROR);
        List<GroupDto.InviteInfo> res = groupService.getInviteMemberList(groupId,"member");
//        GroupDto.res res=new GroupDto.res(inviteMemberList);
        return new ResponseEntity<>(res,HttpStatus.OK);
    }
    @GetMapping("/getInviteList/{groupId}")
    @Operation(summary = "그룹장이 해당 그룹에서 보낸 초대 목록 확인",description = "그룹장이 보낸 초대 목록을 확인하는 API(그룹장 Only)")
    public ResponseEntity<List<GroupDto.InviteInfo>> getInviteList(@PathVariable Long groupId){
        if(!groupService.checkOwner(groupId))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        List<GroupDto.InviteInfo> res = groupService.getInviteMemberList(groupId,"group");
//        GroupDto.res res=new GroupDto.res(inviteMemberList);
        return new ResponseEntity<>(res,HttpStatus.OK);
    }


    @PostMapping("/getMemberTimeTable")
    @Operation(summary = "그룹원 시간표 조회하기", description = "그룹원 시간표를 조회하는 API")
    @Parameter(
            name = "groupMemberId",
            description = "대상이 되는 그룹원의 ID를 넘김"
    )
    public ResponseEntity<TimeTableDto.TimeTableInfo> getGroupMemberTimeTable(final @RequestParam Long groupMemberId , @RequestBody TimeTableDto.TimeTableRequestForm requestForm){
        //Long currentMemberId = memberService.getCurrentMemberId();
        //log.info("build: " + requestForm.toString());
        TimeTableDto.TimeTableInfo timeTableInfo
                = timeTableService.getMemberTimeTableByDayNum(groupMemberId, requestForm.getStartDate(), requestForm.getEndDate(), false);
        return new ResponseEntity<>(timeTableInfo, HttpStatus.OK);
    }
    @GetMapping("/isOwner/{groupId}")
    @Operation(summary = "그룹장 확인", description = "해당 그룹의 그룹장인지 확인하는 API")
    public ResponseEntity<Boolean> checkOwner(@PathVariable Long groupId){
        return new ResponseEntity<>(groupService.checkOwner(groupId),HttpStatus.OK);
    }
    @PutMapping("/changeOwner")
    @Operation(summary = "그룹의 그룹장을 변경", description = "그룹의 그룹장이 다른 회원을 그룹장으로 임명하는 API")
    public ResponseEntity<String> changeOwner(@RequestBody GroupDto.memberGroupReq req){
        String name = groupService.changeOwner(req);
        return new ResponseEntity<>("New Owner : "+name,HttpStatus.OK);
    }


    //TODO : 그룹 일정 수정 API (미래 일정만 가능하게, 삭제 후 새로 생성하든 뭐..), 그룹 일정 삭제 API
}
