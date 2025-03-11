package com.c202.userservice.domain.user.model.response;

import com.c202.userservice.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {

    private Long id;
    private String username;
    private String nickname;
    private String birthDate;
    
    // Dto 변환
    public static UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getUserSeq())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .build();
    }
}
