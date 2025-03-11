//package com.c202.userservice.global.auth.blacklist;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.Date;
//import java.util.concurrent.TimeUnit;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class AccessTokenBlacklistService {
//
//    private final RedisTemplate<String, String> redisTemplate;
//
//    private static final String BLACKLIST_PREFIX = "token:blacklist:";
//
//    /**
//     * 액세스 토큰을 블랙리스트에 추가합니다.
//     * @param token 블랙리스트에 추가할 액세스 토큰
//     * @param expirationTime 토큰의 만료 시간 (밀리초)
//     */
//    public void addToBlacklist(String token, Date expirationTime) {
//        try {
//            long ttl = expirationTime.getTime() - System.currentTimeMillis();
//            if (ttl > 0) {
//                String key = BLACKLIST_PREFIX + token;
//                redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
//                log.info("토큰이 블랙리스트에 추가되었습니다. 남은 시간: {}ms", ttl);
//            } else {
//                log.info("이미 만료된 토큰입니다. 블랙리스트에 추가하지 않습니다.");
//            }
//        } catch (Exception e) {
//            log.error("토큰 블랙리스트 추가 중 오류 발생: {}", e.getMessage());
//        }
//    }
//
//    /**
//     * 토큰이 블랙리스트에 있는지 확인합니다.
//     * @param token 확인할 토큰
//     * @return 블랙리스트에 있으면 true, 없으면 false
//     */
//    public boolean isBlacklisted(String token) {
//        try {
//            String key = BLACKLIST_PREFIX + token;
//            boolean isBlacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(key));
//            if (isBlacklisted) {
//                log.debug("토큰이 블랙리스트에 있습니다: {}", token.substring(0, 10));
//            }
//            return isBlacklisted;
//        } catch (Exception e) {
//            log.error("토큰 블랙리스트 확인 중 오류 발생: {}", e.getMessage(), e);
//            return false;
//        }
//    }
//}