package com.example.emptySaver.service;

import com.example.emptySaver.config.jwt.SecurityUtil;
import com.example.emptySaver.domain.dto.FCMDto;
import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.MemberRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FCMService {
    private final FirebaseMessaging firebaseMessaging;
    private final MemberRepository memberRepository;
    public void sendMessageToMember(Long memberId,String title, String body){
        Notification notification=Notification.builder()
                .setTitle(title).setBody(body).build();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_USERID));
        Message message=Message.builder().setToken(member.getFcmToken())
                .setNotification(notification).build();
        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            throw new BaseException(BaseResponseStatus.FAILED_TO_SEND_NOTIFICATION);
        }
    }

    public void sendMessageToMemberList(List<Long> memberIdList, String title, String body){
        for (Long memberId : memberIdList) {
            this.sendMessageToMember(memberId,title,body);
        }
    }
}
