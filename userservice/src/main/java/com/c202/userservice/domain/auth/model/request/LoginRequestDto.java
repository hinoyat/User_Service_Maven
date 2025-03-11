package com.c202.userservice.domain.auth.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequestDto {

    private String username;
    private String password;
}
