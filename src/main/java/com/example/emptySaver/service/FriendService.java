package com.example.emptySaver.service;

import com.example.emptySaver.config.jwt.SecurityUtil;
import com.example.emptySaver.domain.dto.FriendDto;
import com.example.emptySaver.domain.entity.Friend;
import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.MemberTeam;
import com.example.emptySaver.domain.entity.Team;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.FriendRepository;
import com.example.emptySaver.repository.MemberRepository;
import com.example.emptySaver.repository.MemberTeamRepository;
import com.example.emptySaver.repository.TeamRepository;
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
public class FriendService {
    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final TeamRepository teamRepository;
    private final FCMService fcmService;
    private Member getMember() {
        String userName = SecurityUtil.getCurrentUsername().orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));
        Member user = memberRepository.findFirstByUsername(userName).orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));
        return user;
    }

    public List<Friend> getFriendEntityList(){
        Member member = getMember();
        return friendRepository.findWithFriendMemberByOwner(member);
    }

    public List<Member> getFriendByMemberEntityList(){  //친구 목록을 member entity로 반환
        List<Member> friendMemberList = new ArrayList<>();

        Member member = getMember();
        List<Friend> withFriendByOwner = friendRepository.findWithFriendMemberByOwner(member);
        for (Friend friend : withFriendByOwner) {
            friendMemberList.add(friend.getFriendMember());
        }

        return friendMemberList;
    }

    public FriendDto.FriendInfo friendToFriendDto(Friend friend){
        if(friend.isFriend())
            return FriendDto.FriendInfo.builder().friendName(friend.getFriendMember().getName())
                    .friendEmail(friend.getFriendMember().getEmail())
                    .friendId(friend.getId()).friendMemberId(friend.getFriendMember().getId())
                    .build();
        return null;
    }

    public List<FriendDto.FriendInfo> getFriendList(){
        Member member = getMember();
        List<Friend> withFriendByOwner = friendRepository.findWithFriendMemberByOwner(member);
        List<FriendDto.FriendInfo> friendInfoList=new ArrayList<>();
        withFriendByOwner.stream().forEach(friend -> {
            if(friend.isFriend())
                friendInfoList.add(FriendDto.FriendInfo.builder().friendName(friend.getFriendMember().getName())
                                .friendEmail(friend.getFriendMember().getEmail())
                                .friendId(friend.getId()).friendMemberId(friend.getFriendMember().getId())
                        .build());
        });
        return friendInfoList;
    }
    public List<FriendDto.FriendInfo> getMySendList(){
        Member member = getMember();
        //현재 회원이 가진 friend 리스트 중 친구가 되지 않은 (false인) 유저를 리턴
        List<Friend> withFriendByOwner = friendRepository.findWithFriendMemberByOwner(member);
        List<FriendDto.FriendInfo> friendInfoList=new ArrayList<>();
        withFriendByOwner.stream().forEach(friend -> {
            if(!friend.isFriend())
                friendInfoList.add(FriendDto.FriendInfo.builder().friendName(friend.getFriendMember().getName())
                        .friendEmail(friend.getFriendMember().getEmail())
                        .friendId(friend.getId()).friendMemberId(friend.getFriendMember().getId()).build());
        });
        return friendInfoList;
    }
    public List<FriendDto.FriendInfo> getReceivedList(){
        Member member = getMember();
        //현재 회원이 보낸 friendMember로 있는 friend리스트 중 친구가 되지 않은(false)인 목록 리턴
        List<Friend> withFriendByOwner = friendRepository.findWithOwnerByFriendMember(member);
        List<FriendDto.FriendInfo> friendInfoList=new ArrayList<>();
        withFriendByOwner.stream().forEach(friend -> {
            if(!friend.isFriend())
                friendInfoList.add(FriendDto.FriendInfo.builder().friendName(friend.getOwner().getName())
                        .friendEmail(friend.getOwner().getEmail())
                        .friendId(friend.getId()).friendMemberId(friend.getOwner().getId()).build());
        });
        return friendInfoList;
    }

    @Transactional
    public void requestFriend(String friendEmail){
        Member member = getMember();
        if(member.getEmail().equals(friendEmail))
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);

        Member target = memberRepository.findFirstByEmail(friendEmail).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_EMAIL));
        Optional<Friend> opt = friendRepository.findFirstByOwnerAndFriendMember(member, target);
        if(opt.isPresent()){
            //이미 친구 요청을 보냈거나 이미 친구이다.
            throw new BaseException(BaseResponseStatus.DUPLICATE_FRIEND_REQUEST);
        }else{
            Friend friend= new Friend();
            friend.addFriendRequest(member,target);
            friendRepository.save(friend);
            fcmService.sendMessageToMember(target.getId(), "친구 추가 요청이 왔습니다",member.getName()+"님이 회원님과 친구를 맺고 싶어 합니다."
                    ,"notification","friend",String.valueOf(friend.getId()));
        }

    }

    @Transactional
    public void removeFriend(Long friendId, boolean forceFlag){
        Friend friend = getFriend(friendId);
        Member member = getMember();
        //해당 친구객체에 연관이 있는 회원만 수정이 가능.
        if(!friend.getOwner().equals(member)&&!friend.getFriendMember().equals(member))
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        if(!forceFlag){
            //이미 친구관계이거나 해당 Friend객체의 대상이 현재 접속한 회원이 아닌 경우 -> 에러
            if(friend.isFriend()|| !friend.getFriendMember().equals(member))
                throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
            else friendRepository.delete(friend);
            //친구 요청을 거절하는 것이므로 그냥 전달받은 friend객체만 지워주면 ok
            //TODO : 뭐랄까 양쪽으로 친구요청상태인 객체가 2 개 존재할 때 이렇게 지우면 하나만 지워진다 ( 즉, 정말 완벽하게 하려면 not friend인 관계
            // 중 friend,owner 가 뒤집힌 관계가 있다면 그것까지 지워야 함.(뭐 지금은 pass)
        }else {
            //양방향 삭제해야 함
            friendRepository.delete(friend);
            Optional<Friend> opt = friendRepository.findFirstByOwnerAndFriendMember(friend.getFriendMember(), member);
            if(opt.isPresent())
                friendRepository.delete(opt.get());
        }
    }
    @Transactional
    public void approveFriend(Long friendId) {
        //즉, b가 owner고 a가 friendMember인 friend의 id를 받아 true로 바꾸고 a가 owner고 b가 friendMember인 friend가 있다면 true로 바꿈
        Friend friend = getFriend(friendId);
        //이미 친구 수락된 상태면 오류 던지고 바로 돌아가자.
        if(friend.isFriend())
            throw new BaseException(BaseResponseStatus.DUPLICATE_FRIEND_REQUEST);
        friend.setFriend(true);

        Member b = friend.getOwner();
        Member a = getMember();
        //만약 b
        System.out.println("b :"+b.getId());
        System.out.println("a :"+a.getId());
        if(a.getId().equals(b.getId()))
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
//        List<Friend> byOwner = friendRepository.findWithFriendMemberByOwner(a);
        //a가 주인인 friend 중에 b가 friendMember인게 있는ㄴ지 확인한다.
//        Optional<Friend> opt = byOwner.stream().filter(fr -> fr.getFriendMember().equals(b)).findAny();
        Optional<Friend> opt = friendRepository.findFirstByOwnerAndFriendMember(a, b);
        //있다면 true로 바꿈 (이미 true여도 상관없이 바꾼다)
        if(opt.isPresent()){
            log.info("a가 b에게 보낸 요청 존재");
            Friend frd = opt.get();
            frd.setFriend(true);
            //둘 다에게 알림 전송
            fcmService.sendMessageToMember(b.getId(),a.getName()+"님과 친구가 되었습니다",a.getName()+"님이 회원님의 친구 요청을 수락하였습니다",
                    "friend","x","x");
            fcmService.sendMessageToMember(a.getId(),b.getName()+"님과 친구가 되었습니다",b.getName()+"님이 회원님의 친구 요청을 수락하였습니다",
                    "friend","x","x");
        }else{
            log.info("a가 b에게 보낸 요청 없음");
            Friend frd = new Friend();
            frd.makeFriend(a,b);
            friendRepository.save(frd);
            fcmService.sendMessageToMember(b.getId(),a.getName()+"님과 친구가 되었습니다",a.getName()+"님이 회원님의 친구 요청을 수락하였습니다",
                    "friend","x","x");
        }

    }

    private Friend getFriend(Long friendId) {
        Friend friend = friendRepository.findById(friendId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_FRIEND_ID));
        return friend;
    }

    public FriendDto.res getNotGroupFriendList(Long groupId) {
        Member member = getMember();
        List<Friend> withFriendByOwner = friendRepository.findWithFriendMemberByOwner(member);

        Team team = teamRepository.findById(groupId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
        List<MemberTeam> withMemberByTeam = memberTeamRepository.findWithMemberByTeam(team);
        List<Member> collect = withMemberByTeam.stream().map(MemberTeam::getMember).collect(Collectors.toList());
        List<FriendDto.FriendInfo> friendInfoList=new ArrayList<>();
        withFriendByOwner.stream().forEach(friend -> {
            if(friend.isFriend()&&!collect.contains(friend.getFriendMember()))
                friendInfoList.add(FriendDto.FriendInfo.builder().friendName(friend.getFriendMember().getName())
                        .friendEmail(friend.getFriendMember().getEmail())
                        .friendId(friend.getId()).friendMemberId(friend.getFriendMember().getId())
                        .build());
        });
        return new FriendDto.res(friendInfoList);
    }
}
