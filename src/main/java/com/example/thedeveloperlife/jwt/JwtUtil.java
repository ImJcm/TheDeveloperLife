package com.example.thedeveloperlife.jwt;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    // 헤더에 저장되는 key 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 토큰 권한 값의 key 값
    public static final String AUTHORIZATION_KEY = "auth";

    // 토큰 식별자
    public static final String BEARER_PREFIX = "Bearer ";
    private final long TOKEN_TIME = 60 * 60 * 1000L; // 60분


    // application.properties 에서 전역변수를 가져와서 사용
    @Value("${jwt.secret.key}")
    private String secretKey;

    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct // 의존성 주입이 이뤄진 후에 실행되는 메서드에 사용한다.
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

//    // 생성된 JWT를 Cookie에 저장
//    public void addJwtToCookie(String token, HttpServletResponse res) {
//        try {
//            //JWT token값에 공백문자가 있으면 안되므로, 공백이 있는 경우 %20 아스키문자로 변환
//            token = URLEncoder.encode(token, "utf-8").replaceAll("\\+", "%20");
//
//            //AUTHTENTICATION_HEADER name으로 JWT token을 cookie로 저장
//            Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token);
//            cookie.setPath("/");
//
//            //Response 객체에 Cookie 추가
//            res.addCookie(cookie);
//        } catch (UnsupportedEncodingException e) {
//        }
//    }

    // 토큰 생성 메서드
    public String createToken(String name) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(name) // 사용자 식별자값
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm)
                        .compact();
    }

    // Header 에서 JWT 가져오기
    public String getJwtFromHeader(HttpServletRequest request) {
//        // request 의 헤더에서 해당 스트링 타입의 키값(Authorization)에 따른 value 를 저장
//        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
//
//        // bearerToken 변수가 null 이 아니고, isEmpty()도 true 가 아니고, char 로 하나하나 빼서 모두 공백이 아니라면
//        // bearerToken 변수가 BEARER_PREFIX 의 값으로 시작한다면
//        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
//            // 전달받은 토큰 값에서 "Bearer " 을 잘라 그 이후의 값을 반환합니다.
//            return bearerToken.substring(7);
//        }
//        return null;

//        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
//        System.out.println(bearerToken);
//        System.out.println(StringUtils.hasText(bearerToken));
//        System.out.println(bearerToken.startsWith(BEARER_PREFIX));
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
//            return bearerToken.substring(7);
//        }
//        return null;

        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AUTHORIZATION_HEADER)) {
                    try {
                        return URLDecoder.decode(cookie.getValue(), "UTF-8"); // Encode 되어 넘어간 Value 다시 Decode
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }


    // 토큰 검증
    public boolean validateToken(String token) {
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Token Error : 토큰 검증 실패");
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Token Error : 토큰 검증 실패");
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Token Error : 토큰 검증 실패");
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("Token Error : 토큰 검증 실패");
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String substringToken(String tokenValue) {
        if(StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new NullPointerException("Not Found Token");
    }

}
