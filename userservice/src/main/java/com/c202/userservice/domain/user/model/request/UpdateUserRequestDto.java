package com.c202.userservice.domain.user.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserRequestDto {

    private String nickname;
    private String password;
    private String birthDate;
    // 아이콘
}
