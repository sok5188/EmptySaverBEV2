package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.AuthDto;
import com.example.emptySaver.domain.dto.AuthDto.SignInForm;
import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional
    public SignInForm join(SignInForm signInForm){
        if(memberRepository.findFirstByEmail(signInForm.getEmail()).orElse(null)!=null)
            throw new BaseException(BaseResponseStatus.POST_USERS_EXISTS_EMAIL);
        String username= signInForm.getEmail().split("@")[0];

        Member build = Member.init().username(username).password(signInForm.getPassword()).classOf(signInForm.getClassOf())
                .name(signInForm.getName()).nickname(signInForm.getNickname()).email(signInForm.getEmail()).build();

        memberRepository.save(build);
        return signInForm;
    }
    public String getUserNameByEmail(String email) {
        Member member = memberRepository.findFirstByEmail(email).orElseThrow(() -> new BaseException(BaseResponseStatus.POST_USERS_EMPTY_EMAIL));
        return member.getUsername();
    }
    @Transactional
    public void setRefreshToken(String username,String refreshJwt) {
        Member member = memberRepository.findFirstByUsername(username).orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));
        member.setRefreshToken(refreshJwt);
    }
}
