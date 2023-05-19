package com.example.emptySaver.domain.dto;

import lombok.Builder;
import lombok.Data;

public class AuthDto {
    @Data
    public static class LoginForm{
        private String email;
        private String password;
        private String fcmToken;
    }
    @Data
    public static class SignInForm{
        private String email;
        private String password;
        private String classOf;
        private String name;
        private String nickname;
    }
    @Data
    public static class findPwdReq{
        private String email;
        private String name;
    }
    @Data
    public static class changePasswordReq{
        private String oldPassword;
        private String newPassword;
    }
    @Data
    @Builder
    public static class MemberInfo{
        private String email;
        private String name;
        private String nickname;
        private String classOf;
    }

    @Data
    @Builder
    public static class SimpleMemberInfo{
        private Long memberId;
        private String name;
        private Boolean isOwner;
    }
}
