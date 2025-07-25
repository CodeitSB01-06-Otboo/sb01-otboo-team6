package com.codeit.sb01otbooteam06.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthUserResponse {
    private String accessToken;
    private String refreshToken;
}
