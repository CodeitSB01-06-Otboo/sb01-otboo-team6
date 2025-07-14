package com.codeit.sb01otbooteam06.domain.weather.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoLocalClient {

    @Value("${kakao.rest-key}")
    private String kakaoKey;

    private final WebClient webClient;

    /**
     * 위·경도를 행정동 계층 배열로 변환
     */
    public List<String> coordToRegion(double lat, double lon) {

        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .scheme("https")
                .host("dapi.kakao.com")
                .path("/v2/local/geo/coord2regioncode.json")
                .queryParam("x", lon)
                .queryParam("y", lat)
                .build())
            .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoKey)
            .retrieve()
            .bodyToMono(KakaoDto.class)
            .map(dto -> dto.documents().stream()
                .filter(d -> "B".equals(d.region_type()))
                .findFirst()
                .map(this::toNames)
                .orElse(List.of()))
            .block();
    }

    /* -------------- 내부 -------------- */
    private List<String> toNames(KakaoDoc d) {
        String depth1 = d.region_1depth_name();
        String depth2 = d.region_2depth_name();
        String depth3 = d.region_3depth_name();
        String depth4 = (d.region_4depth_name() == null || d.region_4depth_name().isBlank()) ? ""
            : d.region_4depth_name();

        return List.of(depth1, depth2, depth3, depth4);
    }

    /* ---------- 응답 DTO ---------- */
    private record KakaoDto(List<KakaoDoc> documents) {

    }

    private record KakaoDoc(
        String region_type,
        String address_name,
        String region_1depth_name,
        String region_2depth_name,
        String region_3depth_name,
        String region_4depth_name) {

    }
}
