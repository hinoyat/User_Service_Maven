package com.c202.userservice.domain.user.controller;

import com.c202.userservice.domain.user.model.request.LoginRequestDto;
import com.c202.userservice.domain.user.model.request.SignupRequestDto;
import com.c202.userservice.domain.user.model.request.UpdateUserRequestDto;
import com.c202.userservice.domain.user.model.response.UserResponseDto;
import com.c202.userservice.domain.user.service.UserService;
import com.c202.userservice.global.auth.CustomUserDetails;
import com.c202.userservice.global.auth.JwtTokenProvider;
import com.c202.userservice.global.auth.TokenDto;
import com.c202.userservice.global.common.response.ApiResponse;
import com.c202.userservice.global.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> signup(@RequestBody SignupRequestDto requestDto) {
        UserResponseDto user = userService.signUp(requestDto);
        return ResponseEntity.ok(ApiResponse.success(user, "회원가입 성공"));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(
            @RequestBody LoginRequestDto requestDto,
            HttpServletResponse response) {

        TokenDto.TokenResponseDto tokens = userService.login(requestDto);

        // 리프레시 토큰을 쿠키에 설정
        jwtTokenProvider.addRefreshTokenToCookie(response, tokens.getRefreshToken());

        // 응답에는 액세스 토큰만 포함
        Map<String, String> tokenResponse = Map.of("accessToken", tokens.getAccessToken());

        return ResponseEntity.ok(ApiResponse.success(tokenResponse, "로그인이 성공했습니다."));
    }

    // 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        // 쿠키에서 리프레시 토큰 추출
        String refreshToken = jwtTokenProvider.extractRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw new ServiceException.AuthenticationException("리프레시 토큰이 없습니다.");
        }

        // 리프레시 토큰으로 새 액세스 토큰 발급
        String newAccessToken = jwtTokenProvider.refreshAccessToken(refreshToken);

        // 응답에 새 액세스 토큰만 포함
        Map<String, String> tokenResponse = Map.of("accessToken", newAccessToken);

        return ResponseEntity.ok(ApiResponse.success(tokenResponse, "토큰이 갱신되었습니다."));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response) {

        // 로그아웃 처리 - 서비스에서 토큰 추출하지 않고 userId만 사용
        userService.logout(userDetails.getId());

        // 리프레시 토큰 쿠키 삭제
        jwtTokenProvider.deleteRefreshTokenCookie(response);

        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃이 성공했습니다."));
    }

    // 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponseDto user = userService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user, "사용자 정보 조회 성공"));
    }

    // 사용자 정보 수정
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateUserRequestDto requestDto) {
        UserResponseDto user = userService.updateUser(userDetails.getUsername(), requestDto);
        return ResponseEntity.ok(ApiResponse.success(user, "사용자 정보 수정 성공"));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "회원 탈퇴 성공"));
    }

    @GetMapping("/check/username/{username}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsernameAvailability(@PathVariable String username) {
        boolean isAvailable = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("available", isAvailable),
                isAvailable ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다."
        ));
    }

    @GetMapping("/check/nickname/{nickname}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkNicknameAvailability(@PathVariable String nickname) {
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("available", isAvailable),
                isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다."
        ));
    }
}