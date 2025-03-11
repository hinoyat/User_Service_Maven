package com.c202.userservice.domain.auth.service;

import com.c202.userservice.domain.user.entity.User;
import com.c202.userservice.domain.auth.model.request.LoginRequestDto;
import com.c202.userservice.domain.auth.model.request.SignupRequestDto;
import com.c202.userservice.domain.user.model.response.UserResponseDto;
import com.c202.userservice.domain.user.repository.UserRepository;
import com.c202.userservice.global.auth.CustomUserDetails;
import com.c202.userservice.global.auth.JwtTokenProvider;
import com.c202.userservice.global.auth.TokenDto;
import com.c202.userservice.global.auth.refreshtoken.RefreshTokenRepository;
import com.c202.userservice.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // 날짜 포맷터
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");

    @Transactional
    public UserResponseDto register(SignupRequestDto request) {
        // 중복 검사
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ServiceException.UsernameAlreadyExistsException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new ServiceException.UsernameAlreadyExistsException("이미 사용 중인 닉네임입니다.");
        }

        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .birthDate(request.getBirthDate())
                .createdAt(now)
                .updatedAt(now)
                .isDeleted("N")
                .build();

        User savedUser = userRepository.save(user);

        return UserResponseDto.toDto(savedUser);
    }

    // 로그인 메소드 - 액세스 토큰과 리프레시 토큰 모두 발급
    public TokenDto.TokenResponseDto login(LoginRequestDto request) {
        // Spring Security 인증 처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 인증된 사용자 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 삭제된 계정인지 확인
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ServiceException.ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if ("Y".equals(user.getIsDeleted())) {
            throw new ServiceException.ResourceNotFoundException("탈퇴한 계정입니다.");
        }

        // 액세스 토큰과 리프레시 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(userDetails.getUsername(), userDetails.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(userDetails.getUsername(), userDetails.getId());

        // 토큰 응답 객체 생성 및 반환
        return TokenDto.TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 로그아웃
    @Transactional
    public void logout(Long userId) {
        log.debug("사용자 ID={}의 로그아웃 처리 시작", userId);

        try {
            // 1. 리프레시 토큰 제거
            refreshTokenRepository.deleteByUserId(userId);
            log.debug("리프레시 토큰 삭제 완료: 사용자 ID={}", userId);
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }


    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

}
