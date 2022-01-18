package com.example.quest.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

//refresh토큰 요청

@Data
public class TokenRefreshRequest {
    @NotBlank
    private String refreshToken;
}
