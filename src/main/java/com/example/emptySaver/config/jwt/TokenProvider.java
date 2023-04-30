package com.example.emptySaver.config.jwt;



import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.repository.MemberRepository;
import com.example.emptySaver.service.CustomUserDetailsService;
import io.jsonwebtoken.*;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
@Component
@Slf4j
public class TokenProvider implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    private static final String AUTHORITIES_KEY = "auth";
    private final String secret;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;
    private Key key;
    private final MemberRepository userRepository;

    private final CustomUserDetailsService userDetailsService;

    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds,
            MemberRepository userRepository, CustomUserDetailsService userDetailsService) {
        this.secret = secret;
        this.accessTokenValidityMs = tokenValidityInSeconds * 1000;
        this.refreshTokenValidityMs=tokenValidityInSeconds*1000*60;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }
    //인증 정보를 받아 토큰을 생성하는 메소드
    public String createToken(Authentication authentication,String type) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        if(type.equals("Access")){
            now+=this.accessTokenValidityMs;
            Date validity = new Date(now);
            log.info("authentication name : "+authentication.getName());
            String compact = Jwts.builder()
                    .setSubject(authentication.getName())
                    .claim(AUTHORITIES_KEY, authorities)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .setExpiration(validity)
                    .compact();
            log.info("compact(jwt)={}",compact);
            return compact;
        }
        else {
            now += this.refreshTokenValidityMs;
            Date validity = new Date(now);
            log.info("authentication name : "+authentication.getName());
            return Jwts.builder()
                    .setSubject(authentication.getName())
                    .signWith(key, SignatureAlgorithm.HS512)
                    .setExpiration(validity)
                    .compact();
        }



    }
    //토큰을 받아 인증 정보를 리턴하는 메소드
    public Authentication getAuthentication(String token, String type) {
        //전달 받은 JWT token을 claim으로 decode한다
        logger.debug("got Token:{}",token);
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Collection<? extends GrantedAuthority> authorities;
        if(type=="ACCESS"){
            authorities =
                    Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            log.info("claims . getsubject : " + claims.getSubject());
            User principal = new User(claims.getSubject(), "", authorities);

            return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
        }else{
            log.info("claims.getSubject() = " + claims.getSubject());
            UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
            return new UsernamePasswordAuthenticationToken(userDetails,token,userDetails.getAuthorities());
        }

    }
    public String getUserName(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public int validateToken(String token) {
        try {
            log.info("now validate token");
            Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            log.info("claimsJws : " + claimsJws.getSignature()+"/"+claimsJws.getBody());
            return 1;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다.");
            return 2;
        } catch (UnsupportedJwtException e) {
            logger.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니다.");
        }
        return 0;
    }
    public boolean refreshValidate(String refreshToken,String name){
        if(this.validateToken(refreshToken)==1){
            //DB에서 꺼내서 같은 객체 인지 확인
            Optional<Member> opt = userRepository.findFirstByUsername(name);
            if(opt.isPresent()){
                return opt.get().getRefreshToken().equals(refreshToken);
            }else{
                return false;
            }
        }else return false;

    }
}
