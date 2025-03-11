package com.c202.userservice.domain.user.service;

import com.c202.userservice.domain.user.model.request.UpdateUserRequestDto;
import com.c202.userservice.domain.user.model.response.UserResponseDto;

public interface UserService {

    UserResponseDto getUserInfo(String username);

    UserResponseDto updateUser(String username, UpdateUserRequestDto request);

    void deleteUser(String username);
}
