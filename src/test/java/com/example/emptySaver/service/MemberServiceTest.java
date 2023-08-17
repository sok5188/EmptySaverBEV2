package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.AuthDto;
import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.MemberTeam;
import com.example.emptySaver.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

@SpringBootTest
@ActiveProfiles("local")
@Slf4j
public class MemberServiceTest {
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @Test
    void join() throws InterruptedException {
        int threadCount=10;
        String email="dododo@uos.ac.kr";

        CopyOnWriteArraySet<String> set=new CopyOnWriteArraySet<>();
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(()->{
//                String email = UUID.randomUUID().toString();
                log.debug("made email: {}",email);
                AuthDto.SignInForm signInForm = new AuthDto.SignInForm();
                signInForm.setPassword("123");
                signInForm.setClassOf("2011");
                signInForm.setName("testUU");
                signInForm.setNickname("mm");
                signInForm.setEmail(email);
                try {
                    memberService.join(signInForm);
                    set.add(email);
                }catch (Exception e){
                    log.info("error !!");
                }finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
//        int errCount=0;
//        for (String s : set) {
//            Optional<Member> opt = memberRepository.findFirstByEmail(s);
//            if(opt.isEmpty()) {
//                errCount++;
//                log.info("{} is not present in repository..", s);
//            }
//        }
//        System.out.println("errCount = " + errCount);
        List<Member> byEmail = memberRepository.findByEmail(email);
        log.info("{} is {}개 만큼 존재",email,byEmail.size());
    }
}
