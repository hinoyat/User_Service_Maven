package com.c202.userservice.domain.auth.service;

import com.c202.userservice.domain.auth.model.request.LoginRequestDto;
import com.c202.userservice.domain.auth.model.request.SignupRequestDto;
import com.c202.userservice.domain.user.model.response.UserResponseDto;
import com.c202.userservice.global.auth.TokenDto;

public interface AuthService {

    UserResponseDto register(SignupRequestDto request);

    TokenDto.TokenResponseDto login(LoginRequestDto request);

    void logout(Long userId);

    boolean isUsernameAvailable(String username);

    boolean isNicknameAvailable(String nickname);
}
