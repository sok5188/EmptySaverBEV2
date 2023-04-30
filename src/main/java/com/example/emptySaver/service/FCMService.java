package com.example.emptySaver.service;

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

@Service
@RequiredArgsConstructor
public class FCMService {
    private final FirebaseMessaging firebaseMessaging;
    private final MemberRepository memberRepository;
    public void sendNotificationByToken(FCMDto fcmDto){
        Member member = memberRepository.findById(fcmDto.getUserId()).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_ID));
        if(member.getFcmToken() == null){
            throw new BaseException(BaseResponseStatus.INVALID_FCMTOKEN);
        }
        Notification notification=Notification.builder().setTitle(fcmDto.getTitle()).setBody(fcmDto.getBody()).build();
        Message message=Message.builder().setToken(member.getFcmToken()).setNotification(notification).build();
        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            throw new BaseException(BaseResponseStatus.FAILED_TO_SEND_NOTIFICATION);
        }
    }
}
