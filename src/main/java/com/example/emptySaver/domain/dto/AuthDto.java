package com.example.emptySaver.domain.dto;

import lombok.Data;

public class AuthDto {
    @Data
    public static class LoginForm{
        private String email;
        private String password;
    }
    @Data
    public static class SignInForm{
        private String email;
        private String password;
        private String classOf;
        private String name;
        private String nickname;
    }
}
