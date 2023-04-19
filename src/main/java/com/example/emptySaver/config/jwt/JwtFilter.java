package com.example.emptySaver.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class JwtFilter extends GenericFilterBean {
    private static final String[] whiteList={"/","/members/add","/auth/*","/css/*","/noAuthOk",
            "/h2-console/*","/swagger-ui/*","/swagger-resources/*","/swagger-resources",
            "/swagger-ui","/swagger-ui.html","/v3/api-docs"};

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_HEADER = "Refresh";
    private TokenProvider tokenProvider;
    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }
    //do Filter는 토큰의 인증정보를 현재 실행중인 security context에 저장하는 역할을 한다
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String jwt = resolveToken(httpServletRequest,1);
        String requestURI = httpServletRequest.getRequestURI();
        logger.info("new Entered :{}",requestURI);
        //화이트리스트는 JWT체크를 하지 않음 (그냥 필터 pass)
        if(httpServletRequest.getCookies()!=null){
            Cookie[] cookies = httpServletRequest.getCookies();
            logger.info("cookies.length = " + cookies.length);
            for (Cookie cookie : cookies) {
                logger.info("cookie = " + cookie);
                logger.info("cookie.getName() = " + cookie.getName());
                logger.info("cookie.getValue() = " + cookie.getValue());
            }
        }
        if(isLoginCheckPath(requestURI)||requestURI.equals("/auth/logout")){
            //AccessToken이 있고 유효한 경우 OK
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)==1) {
                Authentication authentication = tokenProvider.getAuthentication(jwt,"ACCESS");
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
            } else if(StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)==2 ){
                //AccessToken 만료
                logger.debug("AccessToken Expired");


                //도메인이 다른 경우 header에 넣어서 사용해야 함.
                String refresh=resolveToken(httpServletRequest,2);
                //도메인이 같은 경우 Cookie에서 RefreshToken으로 값 찾아서 바로 사용하면 됨
                Optional<Cookie> optionalCookie = Arrays.stream(httpServletRequest.getCookies())
                        .filter(cookie -> cookie.getName().equals("RefreshToken")).findAny();
                if(optionalCookie.isPresent()){
                    refresh=optionalCookie.get().getValue();
                    System.out.println("refresh by Cookie = " + refresh);
                    logger.info("cookie exist? :{}",httpServletRequest.getCookies().length);
                }else{
                    refresh=resolveToken(httpServletRequest,2);
                    System.out.println("refresh by Header= " + refresh);
                }

                if(tokenProvider.validateToken(refresh)==1){
                    String name = tokenProvider.getUserName(refresh);
                    if(tokenProvider.refreshValidate(refresh,name)){
                        //Refresh Token 정상
                        logger.debug("Re-New Access Token");
                        Authentication authentication = tokenProvider.getAuthentication(refresh,"REFRESH");
                        String accessToken = tokenProvider.createToken(authentication, "Access");
                        logger.debug("New Access Token:{}",accessToken);

                        response.setHeader(AUTHORIZATION_HEADER,accessToken);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }else{
                        //Refresh Token 사용불가
                        logger.debug("접근 토큰 만료, 리프레시 토큰 사용 불가, uri: {} url : {}", requestURI, httpServletRequest.getRequestURL());
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }
            }else{
                //AccessToken,Refresh Token 사용불가
                logger.debug("유효한 JWT 토큰이 없습니다, uri: {} url : {}", requestURI, httpServletRequest.getRequestURL());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }else{
            logger.info("Received:{} is whiteList",requestURI);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
    //request헤더에서 토큰을 받아오는 메소드
    private String resolveToken(HttpServletRequest request, int type) {
        //1 : access 2: refresh
        logger.debug("in resolve token Type:{}",type);
        String bearerToken;
        if(type==1)
            bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        else    bearerToken=request.getHeader(REFRESH_HEADER);
        System.out.println("bearerToken:"+bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
    private boolean isLoginCheckPath(String requestURI){
        return !PatternMatchUtils.simpleMatch(whiteList,requestURI);
    }
}
