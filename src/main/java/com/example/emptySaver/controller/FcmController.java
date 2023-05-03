package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.FCMDto;
import com.example.emptySaver.service.FCMService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
@Slf4j
public class FcmController {
    private final FCMService fcmService;
    @Operation(summary = "알림 전송", description = "현재 로그인 된 유저의 fcm토큰을 이용하여 알림 보내기")
    @GetMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody FCMDto fcmDto){
        fcmService.sendNotificationByToken(fcmDto);
        return ResponseEntity.ok("Send Notification");
    }
}
