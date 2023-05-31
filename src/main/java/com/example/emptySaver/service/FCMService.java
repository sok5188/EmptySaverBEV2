package com.example.emptySaver.service;

import com.example.emptySaver.config.jwt.SecurityUtil;
import com.example.emptySaver.domain.dto.FCMDto;
import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.MemberRepository;
import com.example.emptySaver.repository.NotificationRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FCMService {
    private final FirebaseMessaging firebaseMessaging;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    public void sendMessageToMember(Long memberId,String title, String body, String routeValue,String idType, String idValue,String idType2, String idValue2){
        Notification notification=Notification.builder()
                .setTitle(title).setBody(body.length()>20?body.substring(0,17)+"...":body).build();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_USERID));
        Message message=Message.builder().setToken(member.getFcmToken()).putData("routeValue",routeValue).putData("idType",idType)
                .putData("idValue",idValue=="x"?"-1":idValue).putData("idType2",idType2).putData("idValue2",idValue2=="x"?"-1":idValue2).setNotification(notification).build();
        com.example.emptySaver.domain.entity.Notification build = com.example.emptySaver.domain.entity.Notification.longInit()
                .member(member).title(title).body(body).routeValue(routeValue)
                .idType(idType).idValue(idValue=="x"?"-1":idValue).idType2(idType2).idValue2(idValue2=="x"?"-1":idValue2).build();
        try {
            log.info("now Send message:"+message.toString());
            firebaseMessaging.send(message);
            notificationRepository.save(build);
        } catch (FirebaseMessagingException e) {
            throw new BaseException(BaseResponseStatus.FAILED_TO_SEND_NOTIFICATION);
        }
    }
    public void sendMessageToMember(Long memberId,String title, String body, String routeValue,String idType, String idValue){
        Notification notification=Notification.builder()
                .setTitle(title).setBody(body.length()>20?body.substring(0,17)+"...":body).build();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_USERID));
        Message message=Message.builder().setToken(member.getFcmToken()).putData("routeValue",routeValue).putData("idType",idType)
                .putData("idValue",idValue=="x"?"-1":idValue).setNotification(notification).build();
        com.example.emptySaver.domain.entity.Notification build = com.example.emptySaver.domain.entity.Notification.init()
                .member(member).title(title).body(body).routeValue(routeValue)
                .idType(idType).idValue(idValue=="x"?"-1":idValue).build();
        try {
            log.info("now Send message:"+message.toString());
            firebaseMessaging.send(message);
            notificationRepository.save(build);
        } catch (FirebaseMessagingException e) {
            throw new BaseException(BaseResponseStatus.FAILED_TO_SEND_NOTIFICATION);
        }
    }

    public void sendMessageToMemberList(List<Long> memberIdList, String title, String body,
                                        String routeValue,String idType, String idValue,String idType2,String idValue2){
        log.info("in fcm send List size: "+memberIdList.size());
        for (Long memberId : memberIdList) {
            log.info("target id:"+memberId);
            this.sendMessageToMember(memberId,title,body,routeValue,idType,idValue,idType2,idValue2);
        }
    }
    public void sendMessageToMemberList(List<Long> memberIdList, String title, String body,
                                        String routeValue,String idType, String idValue){
        log.info("in fcm send List size: "+memberIdList.size());
        for (Long memberId : memberIdList) {
            log.info("target id:"+memberId);
            this.sendMessageToMember(memberId,title,body,routeValue,idType,idValue);
        }
    }
}
