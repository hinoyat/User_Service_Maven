package com.c202.userservice.domain.user.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignupRequestDto {

    private String username;
    private String password;
    private String nickname;
    private String birthDate;
    private String birthTime;
}
