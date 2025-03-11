package com.c202.userservice.domain.user.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateIntroductionDto {

    private String introduction;
}
