package com.example.emptySaver.service;

import com.example.emptySaver.config.jwt.SecurityUtil;
import com.example.emptySaver.domain.dto.AuthDto;
import com.example.emptySaver.domain.dto.AuthDto.SignInForm;
import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.MemberTeam;
import com.example.emptySaver.domain.entity.Team;
import com.example.emptySaver.domain.entity.Time_Table;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.MemberRepository;
import com.example.emptySaver.repository.MemberTeamRepository;
import com.example.emptySaver.repository.TeamRepository;
import com.example.emptySaver.repository.TimeTableRepository;
import com.example.emptySaver.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final TimeTableRepository timeTableRepository;
    private final BCryptPasswordEncoder encoder;
    private final TeamRepository teamRepository;
    private final MemberTeamRepository memberTeamRepository;

    @Transactional
    public SignInForm join(SignInForm signInForm){
//        if(memberRepository.existsByEmail(signInForm.getEmail())) {
//            log.debug("already exist.. {}"+signInForm.getEmail());
//            throw new BaseException(BaseResponseStatus.POST_USERS_EXISTS_EMAIL);
//        }

        String username= signInForm.getEmail().split("@")[0];


        Member build = Member.init().username(username).password(signInForm.getPassword()).classOf(signInForm.getClassOf())
                .name(signInForm.getName()).nickname(signInForm.getNickname()).email(signInForm.getEmail()).build();
        try {
            memberRepository.save(build);
        }catch (DataIntegrityViolationException e){
            if(e.getMessage().toUpperCase().contains(Constant.Constraint.EMAIL_UNIQUE.toUpperCase())){
                log.debug("already exist.. {}"+signInForm.getEmail());
                throw new BaseException(BaseResponseStatus.POST_USERS_EXISTS_EMAIL);
            }
            throw  e;
        }

        Time_Table timeTable = Time_Table.builder().member(build).build();
        timeTableRepository.save(timeTable);
        build.setTimeTable(timeTable);
        return signInForm;
    }
    public String getUserNameByEmail(String email) {
        Member member = memberRepository.findFirstByEmail(email).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_EMAIL));
        return member.getUsername();
    }
    @Transactional
    public void setRefreshToken(String username,String refreshJwt) {
        Member member = memberRepository.findFirstByUsername(username).orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));
        member.setRefreshToken(refreshJwt);
    }

    @Transactional
    public void deleteRefresh() {
        Member user = getMember();
        user.setRefreshToken("");
    }

    public Member getMember() {
        String userName = SecurityUtil.getCurrentUsername().orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));
        Member user = memberRepository.findFirstByUsername(userName).orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));
        return user;
    }

    public Long getCurrentMemberId(){
        Member member = getMember();
        return member.getId();
    }
    public Member getMemberByEmail(String email){
        Member member = memberRepository.findFirstByEmail(email).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_EMAIL));
        return member;
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_DELETE_ATTEMPT));
        List<Team> byOwner = teamRepository.findByOwner(member);
        byOwner.stream().forEach(team -> {
            List<MemberTeam> memberByTeam = memberTeamRepository.findWithMemberByTeam(team);
            if(memberByTeam.size()==1){
                //그룹장인 자신만 존재 -> 그룹삭제
                teamRepository.delete(team);
            }else{
                //다른 회원 존재-> 그룹장 위임
                MemberTeam memberTeam = memberByTeam.stream().filter(mt -> !mt.getMember().equals(member)).findAny().orElseThrow(() -> new BaseException(BaseResponseStatus.RESPONSE_ERROR));
                team.setOwner(memberTeam.getMember());
            }
        });
        memberRepository.delete(member);
    }
    public String makeRandomString(){
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    public String resetPassword(AuthDto.findPwdReq findPwdReq) {
        Member member = memberRepository.findFirstByEmail(findPwdReq.getEmail()).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_EMAIL));
        if(!member.getName().equals(findPwdReq.getName())){
            throw new BaseException(BaseResponseStatus.INVALID_CHANGE_ATTEMPT_NAME);
        }
        String newPassword = makeRandomString();
        member.setPassword(encoder.encode(newPassword));


        return newPassword;
    }
    @Transactional
    public void changePassword(AuthDto.changePasswordReq passwordReq){
        Member member = getMember();
        if(!encoder.matches(passwordReq.getOldPassword(),member.getPassword()))
            throw new BaseException(BaseResponseStatus.INVALID_PASSWORD);
        member.setPassword(encoder.encode(passwordReq.getNewPassword()));
    }

    @Transactional
    public void setFCMToken(String username,String fcmToken) {
        Member member = memberRepository.findFirstByUsername(username).orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));
        member.setFcmToken(fcmToken);
    }

    @Transactional
    public void changeNickName(String newNickName) {
        Member member= this.getMember();
        member.setNickname(newNickName);
    }

    public AuthDto.MemberInfo getMemberInfo() {
        Member member= this.getMember();
        return AuthDto.MemberInfo.builder().email(member.getEmail()).name(member.getName()).nickname(member.getNickname())
                .classOf(member.getClassOf()).build();
    }
}
