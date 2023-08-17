package com.example.emptySaver.service;

import com.example.emptySaver.config.jwt.SecurityUtil;
import com.example.emptySaver.config.jwt.TokenProvider;
import com.example.emptySaver.domain.dto.AuthDto;
import com.example.emptySaver.domain.dto.CommentDto;
import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@ActiveProfiles("local")
@Slf4j
public class BoardServiceTest {
    @Autowired
    MemberService memberService;
    @Autowired
    BoardService boardService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupService groupService;
    @Test
    void 가입후댓글남기기() throws InterruptedException {
        int threadCount=10;

        CopyOnWriteArraySet<String> set=new CopyOnWriteArraySet<>();
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(()->{
                String email = UUID.randomUUID().toString();
                log.debug("made email: {}",email);
                AuthDto.SignInForm signInForm = new AuthDto.SignInForm();
                signInForm.setPassword("123");
                signInForm.setClassOf("2011");
                signInForm.setName("testUU");
                signInForm.setNickname("mm");
                signInForm.setEmail(email);
                try {
                    //join
                    memberService.join(signInForm);
                    set.add(email);
                    //login
                    Member member = memberService.getMemberByEmail(email);
                    memberService.setFCMToken(member.getUsername(),"djDBQUAQTcy-Q7yn5Y_uZG:APA91bHrdu3OC_7EK_XS-UnepZs7H0Z29wcphC2JRqrwG-XHWNOwAIPXJoSOUpGc9RbeCDshPryJJXG8Mry_YA2WyXYh06epNUaJkGBeLQcHXK9wU7pEfhtdTuEnaVAL6cSJ60p5Y29v");
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(member.getUsername(), "123");
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    //send Request
                    groupService.addMemberToTeam(member.getId(), 1L,"member");
                    //accept request
                    groupService.acceptMember(member.getId(),1L);
                    //add comment
                    CommentDto.PostCommentAddReq req = CommentDto.PostCommentAddReq.builder().groupId(1L).parentCommentId(-1L).postId(1L).text("cmc").build();
                    boardService.addCommentToPost(req,member.getId());
                    //view post
                    boardService.getPostDetail(1L,member.getId());
                }catch (Exception e){
                    log.info("error !!");
                }finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();


        int errCount=0;
        for (String s : set) {
            Optional<Member> opt = memberRepository.findFirstByEmail(s);
            if(opt.isEmpty()) {
                errCount++;
                log.info("{} is not present in repository..", s);
            }
        }
        System.out.println("errCount = " + errCount);
    }
}
