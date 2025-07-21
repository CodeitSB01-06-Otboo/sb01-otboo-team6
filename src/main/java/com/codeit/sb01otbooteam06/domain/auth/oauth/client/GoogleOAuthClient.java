package com.codeit.sb01otbooteam06.domain.auth.oauth.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.HashMap;
import java.util.Map;

@Component
public class GoogleOAuthClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String clientId = "YOUR_GOOGLE_CLIENT_ID";
    private final String clientSecret = "YOUR_GOOGLE_CLIENT_SECRET";
    private final String redirectUri = "YOUR_GOOGLE_REDIRECT_URI";

    public Map<String, Object> getUserInfo(String code) {
        //토큰 요청
        Map<String, String> tokenRequest = new HashMap<>();
        tokenRequest.put("code", code);
        tokenRequest.put("client_id", clientId);
        tokenRequest.put("client_secret", clientSecret);
        tokenRequest.put("redirect_uri", redirectUri);
        tokenRequest.put("grant_type", "authorization_code");

        Map<String, Object> tokenResponse = restTemplate.postForObject(
                "https://oauth2.googleapis.com/token",
                tokenRequest,
                Map.class
        );

        String accessToken = (String) tokenResponse.get("access_token");

        //사용자 정보 요청
        String uri = UriComponentsBuilder.fromUriString("https://www.googleapis.com/oauth2/v2/userinfo")
                .queryParam("access_token", accessToken)
                .toUriString();

        return restTemplate.getForObject(uri, Map.class);
    }
}
