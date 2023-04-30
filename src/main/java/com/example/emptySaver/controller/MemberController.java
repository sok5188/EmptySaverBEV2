package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.AuthDto;
import com.example.emptySaver.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/afterAuth")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    private final MemberService memberService;
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "회원 로그아웃 처리")
    public ResponseEntity<String> logout(HttpServletResponse response){
        log.info("logout called");
        memberService.deleteRefresh();
        expireCookie(response,"RefreshToken");
        return new ResponseEntity<>("Logout Success", HttpStatus.OK);
    }

    //TODO:백오피스 만들면 role이 관리자인 경우만 접근 가능한 API로 수정
    @DeleteMapping("/delete/{memberId}")
    @Operation(summary = "회원삭제", description = "특정 회원을 삭제하는 API")
    public ResponseEntity<String> deleteMember(@PathVariable("memberId") Long memberId){
        memberService.deleteMember(memberId);
        return new ResponseEntity<>("User Deleted By Manager",HttpStatus.OK);
    }
    @DeleteMapping("/deleteme")
    @Operation(summary = "회원탈퇴", description = "회원이 탈퇴하는 경우 사용하는 API")
    public ResponseEntity<String> deleteMember(){
        Long currentMemberId = memberService.getCurrentMemberId();
        memberService.deleteMember(currentMemberId);
        return new ResponseEntity<>("User(Client) Deleted",HttpStatus.OK);
    }
    @PutMapping("/changePassword")
    @Operation(summary = "비밀번호 변경", description = "회원이 비밀번호를 변경하는 API")
    public ResponseEntity<String> changePassword(@RequestBody AuthDto.changePasswordReq pwdReq){
        memberService.changePassword(pwdReq);
        return new ResponseEntity<>("User Password Updated",HttpStatus.OK);
    }
    @PutMapping("/changeNickName/{newNickName}")
    @Operation(summary = "별명 변경", description = "사용자 별명을 변경하는 API")
    public ResponseEntity<String> changeNickName(@PathVariable String newNickName){
        memberService.changeNickName(newNickName);
        return new ResponseEntity<>("User NickName Updated",HttpStatus.OK);
    }
    @GetMapping("/getMemberInfo")
    @Operation(summary = "회원정보 조회", description = "회원 정보 조회 API")
    public ResponseEntity<AuthDto.MemberInfo> getMemberInfo(){
        return new ResponseEntity<AuthDto.MemberInfo>(memberService.getMemberInfo(),HttpStatus.OK);
    }



    private static void expireCookie(HttpServletResponse response,String name) {
        Cookie cookie=new Cookie(name, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
