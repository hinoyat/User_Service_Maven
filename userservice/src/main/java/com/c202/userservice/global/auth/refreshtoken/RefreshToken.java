package com.c202.userservice.global.auth.refreshtoken;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 ID와 매핑
    @Column(nullable = false)
    private Long userId;

    // 토큰 값 저장
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    // 토큰 만료 시간
    @Column(nullable = false)
    private String expiryDate;

    // 토큰 값 업데이트 메소드
    public void updateToken(String token, String expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }
}