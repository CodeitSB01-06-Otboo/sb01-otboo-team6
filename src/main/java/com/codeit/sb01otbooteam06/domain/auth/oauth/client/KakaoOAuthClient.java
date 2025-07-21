package com.codeit.sb01otbooteam06.domain.auth.oauth.client;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Component
public class KakaoOAuthClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String clientId = "YOUR_KAKAO_CLIENT_ID";
    private final String redirectUri = "YOUR_KAKAO_REDIRECT_URI";

    public Map<String, Object> getUserInfo(String code) {
        // 토큰 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&code=" + code;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                request,
                Map.class
        );

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 사용자 정보 요청
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userInfoRequest = new HttpEntity<>(authHeaders);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                userInfoRequest,
                Map.class
        );

        return userInfoResponse.getBody();
    }
}
