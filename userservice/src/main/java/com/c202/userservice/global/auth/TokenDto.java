package com.c202.userservice.global.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TokenDto {

    // 토큰 요청 DTO (리프레시 토큰으로 새 액세스 토큰 요청)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequestDto {
        private String refreshToken;
    }

    // 토큰 응답 DTO (로그인 시 액세스 토큰, 리프레시 토큰 반환)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenResponseDto {
        private String accessToken;
        private String refreshToken;
    }
}
