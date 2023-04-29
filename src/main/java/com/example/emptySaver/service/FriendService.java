package com.example.emptySaver.service;

import com.example.emptySaver.config.jwt.SecurityUtil;
import com.example.emptySaver.domain.dto.FriendDto;
import com.example.emptySaver.domain.entity.Friend;
import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.FriendRepository;
import com.example.emptySaver.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FriendService {
    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private Member getMember() {
        String userName = SecurityUtil.getCurrentUsername().orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));
        Member user = memberRepository.findFirstByUsername(userName).orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));
        return user;
    }
    public List<FriendDto.FriendInfo> getFriendList(){
        Member member = getMember();
        List<Friend> withFriendByOwner = friendRepository.findWithFriendByOwner(member);
        List<FriendDto.FriendInfo> friendInfoList=new ArrayList<>();
        withFriendByOwner.stream().forEach(friend -> friendInfoList.add(FriendDto.FriendInfo.builder()
                .friendName(friend.getFriendMember().getName()).friendId(friend.getFriendMember().getId()).build()));
        return friendInfoList;
    }
}
