package com.codeit.sb01otbooteam06.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private boolean mustChangePassword; // 추가: 임시 비밀번호 사용 여부
}
