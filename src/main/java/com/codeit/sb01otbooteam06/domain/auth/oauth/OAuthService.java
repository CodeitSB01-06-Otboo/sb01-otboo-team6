package com.codeit.sb01otbooteam06.domain.auth.oauth;

import com.codeit.sb01otbooteam06.domain.auth.dto.OAuthUserResponse;

public interface OAuthService {
    OAuthUserResponse getUserInfo(String provider, String code);
}
