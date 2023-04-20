package com.example.emptySaver.service;

import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
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

    private MimeMessage createMessage(String target, String detail, String title) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = sender.createMimeMessage();
        message.addRecipients(Message.RecipientType.TO,target);
        message.setSubject(title);
        message.setText(detail, "utf-8", "html");
        message.setFrom(new InternetAddress("sok980808@daum.net","공강구조대"));
        return message;
    }
    public String createCode(){
        String s = UUID.randomUUID().toString();
        return s.substring(0,Math.min(8,s.length()));
    }

    public String sendMessage(String target, String text,String title){
        String code = createCode();
        try {
            MimeMessage message = createMessage(target,text,title);
            sender.send(message);
        }catch (Exception e){
            log.error("메일 전송 오류 발생");
            throw new BaseException(BaseResponseStatus.FAILED_TO_SEND_MAIL);
        }
        return code;
    }
}
