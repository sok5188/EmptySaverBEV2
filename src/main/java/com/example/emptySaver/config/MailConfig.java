package com.example.emptySaver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    private final String mailId, password;
    public MailConfig(@Value("${mail.id}") String mailId, @Value("${mail.password}") String password) {
        this.mailId=mailId;
        this.password=password;
    }
    @Bean
    public JavaMailSender javaMailSender(){
        JavaMailSenderImpl javaMailSender=new JavaMailSenderImpl();
        javaMailSender.setHost("smtp.daum.net");
        javaMailSender.setUsername(mailId);
        javaMailSender.setPassword(password);
        javaMailSender.setPort(465);
        javaMailSender.setJavaMailProperties(getMailProperties());
        return javaMailSender;
    }

    private Properties getMailProperties() {
        Properties properties=new Properties();
        properties.setProperty("mail.transport.protocol","smtp");
        properties.setProperty("mail.smtp.auth","true");
        properties.setProperty("mail.smtp.starttls.enable","true");
        properties.setProperty("mail.debug","true");
        properties.setProperty("mail.smtp.ssl.trust","smtp.daum.net");
        properties.setProperty("mail.smtp.ssl.enable","true");
        return properties;

    }
}
