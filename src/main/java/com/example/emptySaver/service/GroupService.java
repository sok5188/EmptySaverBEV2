package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.AuthDto;
import com.example.emptySaver.domain.dto.CommentDto;
import com.example.emptySaver.domain.dto.GroupDto;
import com.example.emptySaver.domain.entity.*;
import com.example.emptySaver.domain.entity.category.Category;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {
    @PersistenceContext
    private final EntityManager em;
    private final CategoryRepository categoryRepository;
    private final TeamRepository teamRepository;
    private final CategoryService categoryService;
    private final MemberRepository memberRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final MemberService memberService;
    private final TimeTableRepository timeTableRepository;
    private final FCMService fcmService;
    private final BoardService boardService;

    @Transactional
    public void addGroup(GroupDto.GroupInfo groupInfo){
//        Category categoryByLabel = categoryService.getCategoryByLabel(groupInfo.getCategoryLabel());
        Category category = categoryService.getListByCategoryAndLabel(groupInfo.getCategoryName(), groupInfo.getLabelName())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_LABEL_NAME));
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
                .isPublic(groupInfo.getIsPublic()).isAnonymous(groupInfo.getIsAnonymous())
                .category(category).owner(member)
                .build();
        teamRepository.save(team);

        Time_Table timeTable = Time_Table.builder().team(team).build();
        timeTableRepository.save(timeTable);
        team.setTimeTable(timeTable);

        MemberTeam mt=new MemberTeam();
        mt.initMemberTeam(member,team,member);
        mt.setBelong(true);
        memberTeamRepository.save(mt);
    }


    @Transactional
    public String addMemberToTeam(Long memberId, Long teamId,String subject){
        //가입신청 or 그룹초대에 사용되는 메소드
        Member member = getMemberById(memberId);
        Team team = getTeamById(teamId);

        memberTeamRepository.findWithMemberByTeam(team).stream()
                .filter(mt->mt.getMember().equals(member)).findAny()
                .ifPresent(memberTeam -> {throw new BaseException(BaseResponseStatus.FAILED_TO_ADD_MEMBER_TO_TEAM);});
        MemberTeam mt=new MemberTeam();
        mt.initMemberTeam(member,team,member);
        //그룹에서 보낸 초대는 group, 회원이 보낸 신청은 member
        mt.setRelationSubject(subject);
        memberTeamRepository.save(mt);
        if(subject.equals("member")){
            //멤버가 그룹에 가인 신청을 하는 경우
            fcmService.sendMessageToMember(team.getOwner().getId(),team.getName()+ " 가입 신청 알림 입니다."
                    ,member.getNickname()+"님이 회원님의 그룹 " + team.getName() + " 에 가입 신청을 하였습니다."
                    );
        }else{
            //그룹장이 회원에게 초대를 보내는 경우
            fcmService.sendMessageToMember(memberId,team.getName()+" 그룹의 초대가 왔습니다",
                    team.getName()+ " 에서 회원님에게 가입 권유를 보냈습니다");
        }
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
        //익명인 경우 조회되지 않게 한다. 즉, isPublic이 true인 경우만 리턴됨
        setSimpleGroupRes(teamRepository.findAll().stream().filter(team -> team.isPublic()).collect(Collectors.toList()),result);
        return result;
    }
    public List<GroupDto.SimpleGroupRes> getGroupByType(String categoryName,String label){
//        Category categoryByLabel = categoryService.getCategoryByLabel(label);
        Category category = categoryService.getListByCategoryAndLabel(categoryName, label).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_LABEL_NAME));
        //익명인 경우 조회되지 않게 한다.
        List<Team> byCategory = teamRepository.findByCategory(category).stream().filter(team -> team.isPublic()).collect(Collectors.toList());

        List<GroupDto.SimpleGroupRes> result = new ArrayList<>();
        setSimpleGroupRes(byCategory, result);
        return result;
    }
    public List<GroupDto.SimpleGroupRes> getGroupByCategoryName(String categoryName){
        List<? extends Category> categoryByName = categoryService.getCategoryByName(categoryName);
        //익명인 경우 조회되지 않게 한다.
        List<Team> byCategory = teamRepository.findWithCategoryByCategoryIn((List<Category>) categoryByName).stream().filter(team -> team.isPublic()).collect(Collectors.toList());
        List<GroupDto.SimpleGroupRes> result = new ArrayList<>();
        setSimpleGroupRes(byCategory, result);
        return result;
    }

    private void setSimpleGroupRes(List<Team> byCategory, List<GroupDto.SimpleGroupRes> result) {

        byCategory.forEach(team -> {
            log.info("in simple group iter");
            System.out.println("team.getCategory().getClass() = " + team.getCategory().getClass());
            result.add(GroupDto.SimpleGroupRes.builder().groupName(team.getName())
                    .groupId(team.getId()).oneLineInfo(team.getOneLineInfo())
                    .nowMember(
                            Long.valueOf(
                                    memberTeamRepository.findByTeam(team).stream().filter(mt->mt.isBelong())
                                            .collect(Collectors.toList()).size()
                            )
                    )
                    .maxMember(team.getMaxMember()).isPublic(team.isPublic()).isAnonymous(team.isAnonymous())
                    .categoryLabel(categoryService.getLabelByCategory(team.getCategory()))
                            .amIOwner(team.getOwner().equals(memberService.getMember()))
                    .build());
        });
    }
    public List<Long> getMyOwnGroupId(){
        Member member = memberService.getMember();
        return teamRepository.findByOwner(member).stream().map(t->t.getId()).collect(Collectors.toList());
    }
    //TODO : (추후 지연로딩 관련 부분 수정해야 할 지점.. (이상해..) 컬렉션 페치조인이 잘 안되는 건가..?
    // 그거 말고도 좀 주의가 필요하다..)
    // 트랜잭션 범위 문제인데, 뭐 나중에 수정하던가..
    public List<GroupDto.SimpleGroupRes> getMyGroup(){
        //익명여부 상관 없이 조회된다.
        List<Team> all = teamRepository.findAll();
        Member member = memberService.getMember();
        List<Team> teamList = memberTeamRepository
                .findWithTeamByMember(member).stream().map(mt->{
                    if(mt.isBelong())
                        return mt.getTeam();
                    else return null;
                }).collect(Collectors.toList());

        List<Team> collect=new ArrayList<>();
        for (Team team : all) {
            System.out.println("team.getCategory().getClass() = " + team.getCategory().getClass());
            if(teamList.contains(team)) {
                collect.add(team);
            }
        }
        List<GroupDto.SimpleGroupRes> result = new ArrayList<>();
        setSimpleGroupRes(collect,result);
        return result;
    }
    //가입되지 않은 상태의 memberTeam중 subject와 맞는 목록을 dto로 변환하여 리턴하는 메소드
    public List<GroupDto.SimpleGroupRes> getGroupRequests(String subject){
        List<Team> collect = this.getGroupListBySubject(subject);
        List<GroupDto.SimpleGroupRes> result = new ArrayList<>();
        setSimpleGroupRes(collect,result);
        return result;
    }
    private List<Team> getGroupListBySubject(String subject){
        List<Team> all = teamRepository.findAll();
        Member member = memberService.getMember();
        List<MemberTeam> mtList = memberTeamRepository.findWithTeamByMember(member);
        List<Team> target = mtList.stream().filter(mt -> !mt.isBelong()&&mt.getRelationSubject().equals(subject)).map(mt -> mt.getTeam()).collect(Collectors.toList());
        List<Team> collect=new ArrayList<>();
        for (Team team : all) {
            if(target.contains(team))
                collect.add(team);
        }

        return collect;
    }


    //해당 그룹에 참가하지 않은 회원중 group에서 초대를 보낸 목록 조회
    public List<GroupDto.InviteInfo> getInviteMemberList(Long groupId,String subject) {
        Team team = this.getTeamById(groupId);
        List<MemberTeam> withMemberByTeam = memberTeamRepository
                .findWithMemberByTeam(team);
        List<GroupDto.InviteInfo> result = new ArrayList<>();
        withMemberByTeam.forEach(mt-> {
            if(!mt.isBelong()&&mt.getRelationSubject().equals(subject)){
                result.add(GroupDto.InviteInfo.builder()
                                .groupId(groupId).groupName(team.getName()).memberTeamId(mt.getId()).memberId(mt.getMember().getId())
                                .memberName(mt.getMember().getName()).inviteDate(mt.getJoinDate())
                        .build()
                );
            }
        });
        return result;
    }

    //가입된 유저만 조회한다.
    public GroupDto.GroupMemberRes getGroupMembers(Long groupId){
        Team team = this.getTeamById(groupId);
        List<MemberTeam> withMemberByTeam = memberTeamRepository
                .findWithMemberByTeam(team);
        List<AuthDto.SimpleMemberInfo> result = new ArrayList<>();
        withMemberByTeam.forEach(mt-> {
                    if(mt.isBelong()){
                        result.add(AuthDto.SimpleMemberInfo.builder()
                                        .memberId(mt.getMember().getId())
                                        .name(mt.getMember().getName())
                                        .isOwner(mt.getMember().equals(team.getOwner()))
                                .build()
                        );
                    }
                });
        GroupDto.GroupMemberRes res= new GroupDto.GroupMemberRes<>(result,team.isAnonymous());
        return res;
    }
    public GroupDto.DetailGroupRes getGroupDetails(Long groupId){
        em.flush();
        em.clear();
        Team team = teamRepository.findFirstWithCategoryById(groupId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
        List<CommentDto.CommentRes> detailComments = boardService.getDetailComments(groupId);
        return GroupDto.DetailGroupRes.builder()
                .groupId(groupId).groupName(team.getName()).oneLineInfo(team.getOneLineInfo())
                .groupDescription(team.getDescription()).nowMember(Long.valueOf(memberTeamRepository.countByTeam(team)))
                .maxMember(team.getMaxMember()).isPublic(team.isPublic()).isAnonymous(team.isAnonymous())
                .categoryLabel(categoryService.getLabelByCategory(team.getCategory()))
                .commentList(detailComments)
                .build();
    }

    public boolean checkOwner(Long groupId){
        Member member = memberService.getMember();
        Team team =this.getTeamById(groupId);
        return team.getOwner().equals(member);
    }
    public boolean checkBelong(Long groupId){
        Member member = memberService.getMember();
        Team team = this.getTeamById(groupId);
        Optional<MemberTeam> opt = memberTeamRepository.findFirstByMemberAndTeam(member, team);
        return opt.isPresent()&&opt.get().isBelong();
    }
    public boolean checkAlone(Long groupId){
        Team team = this.getTeamById(groupId);
        List<MemberTeam> byTeam = memberTeamRepository.findByTeam(team);
        if(byTeam.size()>1)
            return false;
        return true;
    }
    @Transactional
    public String acceptMember(Long memberId, Long groupId){
        //TODO: 만약 뭐 여기서도 가입 승인 Or 가입 권유 ok응답 이렇게 가입되는 경우 요청자에게 알림을 보내는게 좋을까??

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_USERID));
        Team team = teamRepository.findById(groupId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
        MemberTeam memberTeam = memberTeamRepository.findFirstByMemberAndTeam(member, team).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_REQUEST));
        //최대 인원 초과 시 수락되지 않아야 한다.
        List<MemberTeam> byTeam = memberTeamRepository.findByTeam(team);
        int size = byTeam.stream().filter(mt -> mt.isBelong()).collect(Collectors.toList()).size();
        if(size>=team.getMaxMember()){
            throw new BaseException(BaseResponseStatus.MAX_MEMBER_ERROR);
        }
        if(memberTeam.isBelong())
            throw new BaseException(BaseResponseStatus.ALREADY_BELONG_ERROR);
        memberTeam.addMemberToTeam();
        return member.getName();
    }

    public boolean checkPublic(Long groupId) {
        Team teamById = this.getTeamById(groupId);
        return teamById.isPublic();
    }


    @Transactional
    public String changeOwner(GroupDto.memberGroupReq req) {

        if(!this.checkOwner(req.getGroupId()))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        Member target = getMemberById(req.getMemberId());
        Team team = getTeamById(req.getGroupId());
        team.setOwner(target);
        return target.getName();
    }
}
