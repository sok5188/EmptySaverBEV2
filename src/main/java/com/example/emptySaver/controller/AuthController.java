package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.LoginDTO;
import com.example.emptySaver.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final MailService mailService;

    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO){


        //do something to return jwt

        return new ResponseEntity<>("ok", null, HttpStatus.OK);
    }
    @PostMapping("/sendEmail")
    @Operation(summary = "이메일 전송", description = "해당 이메일로 인증 코드를 전송하고 코드를 반환해주는 API")
    public ResponseEntity<String> sendEmail(@RequestParam String email){
        log.info("email : "+email);
        String code = mailService.sendMessage(email);
        return new ResponseEntity<>(code,null,HttpStatus.OK);
    }
}
