package com.example.emptySaver.config;

import com.example.emptySaver.config.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;
import java.util.List;

@Configuration
public class CorsConfig {
   @Bean
   public CorsFilter corsFilter() {
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowCredentials(true);
      config.setAllowedMethods(List.of(new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"}));
      config.addAllowedOrigin("http://localhost:3000");
      config.addAllowedOrigin("http://172.30.1.48:3000");
      config.addAllowedOrigin("http://localhost:8080");
      config.addAllowedOrigin("http://3.34.232.63:3000");

      config.setAllowedHeaders(Collections.singletonList("*"));
      config.addExposedHeader(JwtFilter.AUTHORIZATION_HEADER);

      source.registerCorsConfiguration("/**", config);
      return new CorsFilter(source);
   }
}
