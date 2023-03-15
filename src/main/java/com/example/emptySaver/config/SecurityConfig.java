package com.example.emptySaver.config;

import com.example.emptySaver.config.jwt.JwtAccessDeniedHandler;
import com.example.emptySaver.config.jwt.JwtAuthenticationEntryPoint;
import com.example.emptySaver.config.jwt.JwtSecurityConfig;
import com.example.emptySaver.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            web.ignoring().requestMatchers("/css/**","/js/**","/images/**");
        };
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration source = new CorsConfiguration();
        source.setAllowCredentials(true);
        source.setAllowedOrigins(Arrays.asList("http://localhost:3000","http://localhost:8080"));
        source.addAllowedOrigin("*");
        source.setAllowedMethods(Arrays.asList("GET","POST"));
        source.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource url= new UrlBasedCorsConfigurationSource();
        url.registerCorsConfiguration("/**", source);

        return url;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .logout().deleteCookies("JSESSIONID").logoutSuccessHandler(((request, response, authentication) -> {
                    //do something
                    //일단 뭐 임시로 홈 리다이렉트? (나중에 따로 핸들러 만들어서 처리 필요)
                    response.sendRedirect("/");
                }))
                .and().apply(new JwtSecurityConfig(tokenProvider))
        ;
        return httpSecurity.build();
    }
}
