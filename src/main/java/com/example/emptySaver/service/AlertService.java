package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.AlertDto;
import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.Notification;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertService {
    private final NotificationRepository notificationRepository;
    private final MemberService memberService;

    public List<AlertDto> getAllNotification(){
        Member member = memberService.getMember();
        List<Notification> byMember = notificationRepository.findByMember(member);
        Collections.sort(byMember);
        List<AlertDto> alertList = new ArrayList<>();
        byMember.forEach(n->alertList.add(AlertDto.builder().id(n.getId()).title(n.getTitle()).body(n.getBody())
                        .routeValue(n.getRouteValue()).idType(n.getIdType()).idValue(n.getIdValue())
                        .idType2(n.getIdType2()==null?"x":n.getIdType2()).idValue2(n.getIdValue2()==null?"-1":n.getIdValue2())
                        .receiveTime(n.getReceiveTime()).isRead(n.getIsRead())
                .build()));
        return alertList;
    }
    @Transactional
    public void checkRead(Long id){
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_NOTIFICATION_ID));
        notification.setIsRead(true);
    }
}
