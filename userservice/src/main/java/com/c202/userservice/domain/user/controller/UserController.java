package com.c202.userservice.domain.user.controller;

import com.c202.userservice.domain.user.model.request.UpdateUserRequestDto;
import com.c202.userservice.domain.user.model.response.UserResponseDto;
import com.c202.userservice.domain.user.service.UserService;
import com.c202.userservice.global.auth.CustomUserDetails;
import com.c202.userservice.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

}