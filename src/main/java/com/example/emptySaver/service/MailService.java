package com.example.emptySaver.service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Service
@Slf4j
public class MailService{
    @Autowired
    JavaMailSender sender;

    private MimeMessage createMessage(String target, String code) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = sender.createMimeMessage();
        message.addRecipients(Message.RecipientType.TO,target);
        message.setSubject("공강구조대 이메일 인증 코드입니다.");
        String detail="";
        detail += "<div style='margin:100px;'>";
        detail += "<h1> 안녕하세요</h1>";
        detail += "<h1> 서울 시립대학교 공강구조대 팀입니다.</h1>";
        detail += "<br>";
        detail += "<p>아래 코드를 복사하여 앱에서 입력해주세요<p>";
        detail += "<br>";
        detail += "<br>";
        detail += "<div align='center' style='border:1px solid black; font-family:verdana';>";
        detail += "<h3 style='color:blue;'>인증 코드입니다.</h3>";
        detail += "<div style='font-size:130%'>";
        detail += "CODE : <strong>";
        detail += code + "</strong><div><br/> "; // 메일에 인증번호 넣기
        detail += "</div>";
        message.setText(detail, "utf-8", "html");
        message.setFrom(new InternetAddress("sok980808@daum.net","공강구조대"));
        return message;
    }
    private String createCode(){
        String s = UUID.randomUUID().toString();
        return s.substring(0,Math.min(8,s.length()));
    }

    public String sendMessage(String target){
        String code = createCode();
        try {
            MimeMessage message = createMessage(target, code);
            sender.send(message);
        }catch (Exception e){
            log.error("메일 전송 오류 발생");
            //TODO : 추후 예외 통합 처리 할 때 여기서 오류 넘겨줘야 함
            throw  new RuntimeException();
        }
        return code;
    }
}
