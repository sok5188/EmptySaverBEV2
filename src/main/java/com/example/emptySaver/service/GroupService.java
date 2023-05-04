package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.AuthDto;
import com.example.emptySaver.domain.dto.GroupDto;
import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.MemberTeam;
import com.example.emptySaver.domain.entity.Team;
import com.example.emptySaver.domain.entity.category.Category;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.CategoryRepository;
import com.example.emptySaver.repository.MemberRepository;
import com.example.emptySaver.repository.MemberTeamRepository;
import com.example.emptySaver.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {
    private final CategoryRepository categoryRepository;
    private final TeamRepository teamRepository;
    private final CategoryService categoryService;
    private final MemberRepository memberRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final MemberService memberService;

    @Transactional
    public void addGroup(GroupDto.GroupInfo groupInfo){
        Category categoryByLabel = categoryService.getCategoryByLabel(groupInfo.getCategoryLabel());
        Member member = memberService.getMember();
        //owner가 같고 제목이 같으면 중복으로 인식하자.. 그리고 여기서 5개 넘기면 최대갯수 제한
        //근데 이걸 할거면 날짜 지났을 때 해당 그룹 정보들을 삭제해야 되는데...
        List<Team> byOwner = teamRepository.findByOwner(member);
        if(byOwner.size()==5){
            throw new BaseException(BaseResponseStatus.FAILED_TO_MAKE_TEAM);
        }
        byOwner.stream().filter(t->t.getName().equals(groupInfo.getGroupName())).findAny()
                .ifPresent(t-> {throw new BaseException(BaseResponseStatus.INVALID_MAKE_TEAM_ATTEMPT);});


        Team team = Team.builder().name(groupInfo.getGroupName()).oneLineInfo(groupInfo.getOneLineInfo())
                .description(groupInfo.getGroupDescription()).maxMember(groupInfo.getMaxMember())
                .isPublic(groupInfo.getIsPublic()).category(categoryByLabel).owner(member)
                .build();
        teamRepository.save(team);
        MemberTeam mt=new MemberTeam();
        mt.initMemberTeam(member,team,member);
        memberTeamRepository.save(mt);
    }

    @Transactional
    public String addMemberToTeam(Long memberId, Long teamId){
        Member member = getMemberById(memberId);
        Team team = getTeamById(teamId);
        memberTeamRepository.findWithMemberByTeam(team).stream()
                .filter(mt->mt.getMember().equals(member)).findAny()
                .ifPresent(memberTeam -> {throw new BaseException(BaseResponseStatus.FAILED_TO_ADD_MEMBER_TO_TEAM);});
        MemberTeam mt=new MemberTeam();
        mt.initMemberTeam(member,team,member);
        memberTeamRepository.save(mt);
        return member.getUsername();
    }
    public Member getMemberById(Long id){
        return memberRepository.findById(id).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_USERID));
    }
    public Team getTeamById(Long id){
        return teamRepository.findById(id).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
    }
    @Transactional
    public void deleteMemberFromGroup(Long memberId, Long groupId){
        Team team = teamRepository.findById(groupId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_USERID));
        if(team.getOwner().equals(member))
            throw new BaseException(BaseResponseStatus.INVALID_TEAM_MODIFY);
        List<MemberTeam> byTeam = memberTeamRepository.findWithMemberByTeam(team);
        MemberTeam memberTeam = byTeam.stream().filter(mt -> mt.getMember().equals(member)).findAny()
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_USERID));

        memberTeamRepository.delete(memberTeam);
    }
    @Transactional
    public void deleteGroup(Long groupId){
        Team team = teamRepository.findById(groupId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
        teamRepository.delete(team);
    }

    public List<GroupDto.SimpleGroupRes> getAllGroup(){
        List<GroupDto.SimpleGroupRes> result=new ArrayList<>();
        setSimpleGroupRes(teamRepository.findAll(),result);
        return result;
    }
    public List<GroupDto.SimpleGroupRes> getGroupByType(String label){
        Category categoryByLabel = categoryService.getCategoryByLabel(label);
        List<Team> byCategory = teamRepository.findByCategory(categoryByLabel);
        List<GroupDto.SimpleGroupRes> result = new ArrayList<>();
        setSimpleGroupRes(byCategory, result);
        return result;
    }

    private void setSimpleGroupRes(List<Team> byCategory, List<GroupDto.SimpleGroupRes> result) {
        byCategory.forEach(team -> {
            result.add(GroupDto.SimpleGroupRes.builder().groupName(team.getName())
                    .groupId(team.getId()).oneLineInfo(team.getOneLineInfo())
                    .nowMember(Long.valueOf(memberTeamRepository.countByTeam(team)))
                    .maxMember(team.getMaxMember()).isPublic(team.isPublic())
                    .categoryLabel(categoryService.getLabelByCategory(team.getCategory()))
                    .build());
        });
    }
    //TODO : 추후 지연로딩 관련 부분 수정해야 할 지점.. (이상해..)
    public List<GroupDto.SimpleGroupRes> getMyGroup(){
        List<Team> all = teamRepository.findAll();
        Member member = memberService.getMember();
        List<Team> teamList = memberTeamRepository
                .findWithTeamByMember(member).stream().map(mt->mt.getTeam()).collect(Collectors.toList());

        List<Team> collect=new ArrayList<>();
        for (Team team : all) {
            if(teamList.contains(team)) {
                collect.add(team);
            }
        }
        List<GroupDto.SimpleGroupRes> result = new ArrayList<>();
        setSimpleGroupRes(collect,result);
        return result;
    }

    public GroupDto.GroupMemberRes getGroupMembers(Long groupId){
        Team team = teamRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
        List<MemberTeam> withMemberByTeam = memberTeamRepository
                .findWithMemberByTeam(team);
        List<AuthDto.SimpleMemberInfo> result = new ArrayList<>();
        withMemberByTeam.forEach(mt->result.add(
                AuthDto.SimpleMemberInfo.builder()
                        .memberId(mt.getMember().getId())
                        .name(mt.getMember().getName()).build()
        ));
        GroupDto.GroupMemberRes res= new GroupDto.GroupMemberRes<>(result,team.isPublic());
        return res;
    }

    public GroupDto.DetailGroupRes getGroupDetails(Long groupId){
        Team team = teamRepository.findWithCategoryById(groupId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
        return GroupDto.DetailGroupRes.builder()
                .groupId(groupId).groupName(team.getName()).oneLineInfo(team.getOneLineInfo())
                .groupDescription(team.getDescription()).nowMember(Long.valueOf(memberTeamRepository.countByTeam(team)))
                .maxMember(team.getMaxMember()).isPublic(team.isPublic()).categoryLabel(categoryService.getLabelByCategory(team.getCategory()))
                .build();
    }
}
