package com.c202.userservice.global.auth;

//import com.c202.userservice.global.auth.blacklist.AccessTokenBlacklistService;
import com.c202.userservice.global.auth.refreshtoken.RefreshToken;
import com.c202.userservice.global.auth.refreshtoken.RefreshTokenRepository;
import com.c202.userservice.global.exception.ServiceException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    // JWT 시크릿 키 (application.properties에서 설정)
    @Value("${jwt.secret-key}")
    private String secretKey;

    // 액세스 토큰 유효 시간 (application.properties에서 설정)
    @Value("${jwt.access-token-validity-in-ms}")
    private long accessTokenValidity;

    // 리프레시 토큰 유효 시간 (application.properties에서 설정)
    @Value("${jwt.refresh-token-validity-in-ms}")
    private long refreshTokenValidity;

    // JWT 서명에 사용할 키
    private Key key;

    // 날짜 포맷터
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // 사용자 상세 정보 서비스
    private final UserDetailsService userDetailsService;

    // 리프레시 토큰 저장소
    private final RefreshTokenRepository refreshTokenRepository;

    // 토큰 블랙리스트
//    private final AccessTokenBlacklistService accessTokenBlacklistService;

    // 초기화 메소드 - 애플리케이션 시작 시 실행
    @PostConstruct
    public void init() {
        // 시크릿 키를 Base64로 인코딩하여 JWT 서명용 키 생성
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        key = Keys.hmacShaKeyFor(encodedKey.getBytes());
    }

    // 액세스 토큰 생성 메소드
    public String createAccessToken(String username, Long userId) {
        // JWT 클레임 설정 - 사용자 식별자와 권한 정보 담기
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId);
        claims.put("type", "access"); // 토큰 타입 지정

        // 현재 시간과 만료 시간 설정
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidity);

        // JWT 토큰 생성 및 반환
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 리프레시 토큰 생성 메소드
    public String createRefreshToken(String username, Long userId) {
        // JWT 클레임 설정
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId);
        claims.put("type", "refresh"); // 토큰 타입 지정

        // 현재 시간과 만료 시간 설정
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidity);

        // JWT 리프레시 토큰 생성
        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 만료 시간을 문자열로 변환
        String expiryDate = LocalDateTime.now().plusNanos(refreshTokenValidity * 1000000).format(DATE_FORMATTER);

        // DB에 리프레시 토큰 저장 또는 업데이트
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(userId);

        if (existingToken.isPresent()) {
            // 기존 토큰이 있으면 업데이트
            RefreshToken tokenEntity = existingToken.get();
            tokenEntity.updateToken(refreshToken, expiryDate);
            refreshTokenRepository.save(tokenEntity);
        } else {
            // 기존 토큰이 없으면 새로 생성
            RefreshToken tokenEntity = RefreshToken.builder()
                    .userId(userId)
                    .token(refreshToken)
                    .expiryDate(expiryDate)
                    .build();
            refreshTokenRepository.save(tokenEntity);
        }

        return refreshToken;
    }

    // 리프레시 토큰을 쿠키에 설정
    public void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);  // JavaScript에서 접근 불가능하게 설정
        cookie.setSecure(true);    // HTTPS에서만 전송 (운영 환경에서 활성화)
        cookie.setPath("/api/auth");  // 쿠키 경로 설정
        cookie.setMaxAge((int) (refreshTokenValidity / 1000));  // 초 단위로 변환

        response.addCookie(cookie);
    }

    // 쿠키에서 리프레시 토큰 추출
    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 리프레시 토큰 쿠키 삭제
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);  // 즉시 만료

        response.addCookie(cookie);
    }

    // JWT 토큰으로부터 인증 객체 생성
    public Authentication getAuthentication(String token) {
        // 토큰에서 사용자명 추출
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        // Spring Security 인증 객체 생성 및 반환
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에서 사용자명 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // 토큰에서 사용자 ID 추출
    public Long getUserId(String token) {
        return ((Number) Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("userId")).longValue();
    }

    // 토큰 유형 확인 (액세스 또는 리프레시)
    public String getTokenType(String token) {
        return (String) Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("type");
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
//            // 블랙리스트
//            if (accessTokenBlacklistService.isBlacklisted(token)) {
//                log.warn("블랙리스트에 등록된 토큰입니다: {}", token);
//                return false;
//            }

            // 토큰 파싱 시도
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

            // 만료 확인
            boolean isValid = !claims.getBody().getExpiration().before(new Date());
            if (isValid) {
                log.debug("유효한 토큰: {}, 사용자: {}", token.substring(0, 10), claims.getBody().getSubject());
            } else {
                log.warn("만료된 토큰: {}", token.substring(0, 10));
            }
            return isValid;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error("유효하지 않은 JWT 서명: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("잘못된 JWT 토큰: {}", e.getMessage());
            return false;
        }
    }

    // 리프레시 토큰으로 새 액세스 토큰 발급
    public String refreshAccessToken(String refreshToken) {
        // 리프레시 토큰 유효성 검증
        if (!validateToken(refreshToken)) {
            throw new ServiceException.AuthenticationException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 토큰 타입 확인
        String tokenType = getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new ServiceException.AuthenticationException("유효한 리프레시 토큰이 아닙니다.");
        }

        // DB에 저장된 리프레시 토큰 확인
        Optional<RefreshToken> savedToken = refreshTokenRepository.findByToken(refreshToken);
        if (savedToken.isEmpty()) {
            throw new ServiceException.AuthenticationException("저장된 리프레시 토큰을 찾을 수 없습니다.");
        }

        // 토큰에서 사용자 정보 추출
        String username = getUsername(refreshToken);
        Long userId = getUserId(refreshToken);

        // 새 액세스 토큰 생성 및 반환
        return createAccessToken(username, userId);
    }

//    // 로그아웃 처리 블랙리스트
//    public void addToBlacklist(String accessToken) {
//        log.debug("토큰 블랙리스트 추가 시작");
//
//        try {
//            // 토큰 유효성 검사
//            if (accessToken == null || accessToken.isEmpty()) {
//                log.warn("블랙리스트에 추가할 토큰이 없습니다.");
//                return;
//            }
//
//            // 토큰에서 만료 시간 추출
//            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
//            Date expiration = claims.getExpiration();
//
//            // 블랙리스트 서비스를 통해 블랙리스트에 추가
//            accessTokenBlacklistService.addToBlacklist(accessToken, expiration);
//            log.debug("액세스 토큰이 블랙리스트에 추가되었습니다. 만료 시간: {}", expiration);
//        } catch (ExpiredJwtException e) {
//            log.warn("이미 만료된 토큰이므로 블랙리스트에 추가하지 않습니다: {}", e.getMessage());
//        } catch (Exception e) {
//            log.error("액세스 토큰 블랙리스트 등록 실패: {}", e.getMessage(), e);
//            // 로그만 남기고 예외를 다시 던지지 않음
//        }
//    }
}