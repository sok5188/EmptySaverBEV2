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

    @PostMapping("/login")
    @Operation(summary = "Login", description = "로그인 성공 시 인증헤더에 접근토큰, 쿠키에 갱신토큰 심어준다.")
    public ResponseEntity<String> login(@RequestBody LoginForm loginDTO, HttpServletResponse response){
        String username = memberService.getUserNameByEmail(loginDTO.getEmail());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, loginDTO.getPassword());
        try{
            log.info("before authenticate");
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            log.info("after authenticate");
            String accessJwt = tokenProvider.createToken(authentication,"Access");
            String refreshJwt = tokenProvider.createToken(authentication,"Refresh");

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(AUTHORIZATION_HEADER, "Bearer " + accessJwt);
            httpHeaders.add(JwtFilter.REFRESH_HEADER,"Bearer "+refreshJwt);
            log.info("in authenticate controller \nACCESS:{} \nREFRESH:{}",accessJwt,refreshJwt);


            memberService.setRefreshToken(username,refreshJwt);

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
    @Operation(summary = "이메일 전송", description = "해당 이메일로 인증 코드를 전송하고 코드를 반환해주는 API")
    public ResponseEntity<String> sendEmail(@RequestParam String email){
        log.info("email : "+email);
        String code = mailService.sendMessage(email);
        return new ResponseEntity<>(code,null,HttpStatus.OK);
    }

    @PostMapping("/signup")
    @Operation(summary = "회원 가입", description = "회원 가입 처리 API(이메일 인증 후)")
    public ResponseEntity<String> signup(@RequestBody SignInForm signInDto){
        signInDto.setPassword(encoder.encode(signInDto.getPassword()));
        SignInForm join = memberService.join(signInDto);
        return new ResponseEntity<>("User SignIn complete"+join.getName(), null, HttpStatus.OK);
    }
}
