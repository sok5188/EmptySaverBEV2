package com.example.emptySaver.config;

import com.example.emptySaver.config.jwt.JwtAccessDeniedHandler;
import com.example.emptySaver.config.jwt.JwtAuthenticationEntryPoint;
import com.example.emptySaver.config.jwt.JwtSecurityConfig;
import com.example.emptySaver.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
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
        source.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        source.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource url= new UrlBasedCorsConfigurationSource();
        url.registerCorsConfiguration("/**", source);

        return url;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().formLogin().disable()
                .httpBasic().disable()

                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                .and()
                .headers().frameOptions().sameOrigin()

                .and()
                .authorizeRequests()
                .requestMatchers("/auth/**").permitAll()
                //TODO: 개발 완료 시 test부분 삭제
                .requestMatchers("/helloTest").permitAll()
                .requestMatchers("/swagger-ui/**","/swagger-ui","/swagger-resources/**","/swagger-resources",
                        "/swagger-ui","/swagger-ui.html","/v3/api-docs","/v3/api-docs/**").permitAll()
                //.requestMatchers(PathRequest.toH2Console()).permitAll()
                .anyRequest().authenticated()
                .and().apply(new JwtSecurityConfig(tokenProvider))
        ;
        return httpSecurity.build();
    }
}
