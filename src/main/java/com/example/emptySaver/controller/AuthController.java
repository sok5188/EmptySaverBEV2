package com.example.emptySaver.controller;

import com.example.emptySaver.config.jwt.JwtFilter;
import com.example.emptySaver.config.jwt.TokenProvider;
import com.example.emptySaver.domain.dto.AuthDto;
import com.example.emptySaver.domain.dto.AuthDto.LoginForm;
import com.example.emptySaver.domain.dto.AuthDto.SignInForm;
import com.example.emptySaver.service.MailService;
import com.example.emptySaver.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import static com.example.emptySaver.config.jwt.JwtFilter.AUTHORIZATION_HEADER;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final MailService mailService;
    private final MemberService memberService;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;

    //TODO: 클라이언트에서 firebase토큰 발급받아서 그걸 유저에 저장해야 한다.
    @PostMapping("/login")
    @Operation(summary = "Login", description = "로그인 성공 시 인증헤더에 접근토큰, 쿠키에 갱신토큰 심어준다.")
    public ResponseEntity<String> login(@RequestBody LoginForm loginDTO, HttpServletResponse response){
        String username = memberService.getUserNameByEmail(loginDTO.getEmail());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, loginDTO.getPassword());
        try{
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            String accessJwt = tokenProvider.createToken(authentication,"Access");
            String refreshJwt = tokenProvider.createToken(authentication,"Refresh");

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(AUTHORIZATION_HEADER, "Bearer " + accessJwt);
            httpHeaders.add(JwtFilter.REFRESH_HEADER,"Bearer "+refreshJwt);
            log.info("in authenticate controller \nACCESS:{} \nREFRESH:{}",accessJwt,refreshJwt);


            memberService.setRefreshToken(username,refreshJwt);
            //FCM token 설정
            memberService.setFCMToken(username,loginDTO.getFcmToken());
            Cookie cookie = new Cookie("RefreshToken",refreshJwt);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            //cookie.setSecure(true);
            response.addCookie(cookie);

            return new ResponseEntity<>(accessJwt, httpHeaders, HttpStatus.OK);
        } catch (Exception e){
            System.out.println(e.getClass());
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    @PostMapping("/sendEmail")
    @Operation(summary = "이메일 인증 코드 전송", description = "해당 이메일로 인증 코드를 전송하고 코드를 반환해주는 API")
    public ResponseEntity<String> sendEmail(@RequestParam("email") String email){
        log.info("email : "+email);
        String code = mailService.createCode();
        String text="";
        text += "<div style='margin:100px;'>";
        text += "<h1> 안녕하세요</h1>";
        text += "<h1> 서울 시립대학교 공강구조대 팀입니다.</h1>";
        text += "<br>";
        text += "<p>아래 코드를 복사하여 앱에서 입력해주세요<p>";
        text += "<br>";
        text += "<br>";
        text += "<div align='center' style='border:1px solid black; font-family:verdana';>";
        text += "<h3 style='color:blue;'>인증 코드입니다.</h3>";
        text += "<div style='font-size:130%'>";
        text += "CODE : <strong>";
        text += code + "</strong><div><br/> "; // 메일에 인증번호 넣기
        text += "</div>";
        mailService.sendMessage(email,text,"공강구조대 이메일 인증 코드 입니다.");
        //String code = mailService.sendMessage(email);
        return new ResponseEntity<>(code,null,HttpStatus.OK);
    }

    @PostMapping("/signup")
    @Operation(summary = "회원 가입", description = "회원 가입 처리 API(이메일 인증 후)")
    public ResponseEntity<String> signup(@RequestBody SignInForm signInDto){
        String encode = encoder.encode(signInDto.getPassword());
        signInDto.setPassword(encode);
        SignInForm join = memberService.join(signInDto);
        return new ResponseEntity<>("User SignIn complete"+join.getName(), null, HttpStatus.OK);
    }
    @PutMapping("/findPassword")
    @Operation(summary = "비밀번호 찾기", description = "비밀번호를 찾기에서 이메일과 이름으로 인증 후 새 비밀번호를 발급하는 API")
    public ResponseEntity<String> findPassword(@RequestBody AuthDto.findPwdReq findPwdReq){
        String resetPassword = memberService.resetPassword(findPwdReq);
        String text="";
        text += "<div style='margin:100px;'>";
        text += "<h1> 안녕하세요</h1>";
        text += "<h1> 서울 시립대학교 공강구조대 팀입니다.</h1>";
        text += "<br>";
        text += "<p>새로 발급된 비밀번호를 복사하여 로그인해주세요<p>";
        text += "<br>";
        text += "<p>로그인 후 꼭 비밀번호를 변경해주세요 !<p>";
        text += "<br>";
        text += "<div align='center' style='border:1px solid black; font-family:verdana';>";
        text += "<h3 style='color:blue;'>임시 비밀번호입니다.</h3>";
        text += "<div style='font-size:130%'>";
        text += "<strong>";
        text += resetPassword + "</strong><div><br/> "; // 메일에 인증번호 넣기
        text += "</div>";
        mailService.sendMessage(findPwdReq.getEmail(),text,"공강구조대 비밀번호 초기화 이메일 입니다.");
        return new ResponseEntity<>("Password has been reset", null, HttpStatus.OK);
    }





}
