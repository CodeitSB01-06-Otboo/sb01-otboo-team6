package com.codeit.sb01otbooteam06.domain.weather.service;

import com.codeit.sb01otbooteam06.domain.weather.dto.KmaVillageItem;
import com.codeit.sb01otbooteam06.domain.weather.dto.KmaVillageResponse;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class KmaApiClient {

    /** 이미 **URL-encoded**( %2B, %3D … ) 된 값을 yml·환경변수에 둡니다 */
    @Value("${kma.service-key}")
    private String encodedServiceKey;          // 예: …%2B…%3D%3D

    private final WebClient webClient;

    public KmaVillageResponse getVillageFcst(int gridX, int gridY) {

        /* 05 시 발표분 날짜 계산 */
        ZoneId kst       = ZoneId.of("Asia/Seoul");
        LocalDate base   = LocalTime.now(kst).isBefore(LocalTime.of(5,0))
            ? LocalDate.now(kst).minusDays(1)
            : LocalDate.now(kst);
        String baseDate  = base.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyymmdd
        String baseTime  = "0500";

        /* ------------------ URI 문자열 ‘직접’ 생성 ------------------ */
        String url = String.format(
            "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst" +
                "?serviceKey=%s&pageNo=1&numOfRows=1000&dataType=JSON" +
                "&base_date=%s&base_time=%s&nx=%d&ny=%d",
            encodedServiceKey, baseDate, baseTime, gridX, gridY);

        log.info("➡️  KMA GET {}", url);

        /* ------------------ 호출 & 매핑 ------------------ */
        return webClient.get()
            .uri(URI.create(url))               // ← 인코딩 건드리지 않음
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(KmaOpenApiDto.class)
            .map(this::toResponse)
            .block();                           // batch 용도로 blocking 허용
    }

    /* -------- JSON → Domain record 변환 -------- */
    private KmaVillageResponse toResponse(KmaOpenApiDto dto) {
        if (dto == null || dto.response() == null
            || dto.response().body() == null
            || dto.response().body().items() == null) {
            return new KmaVillageResponse(List.of());
        }
        List<KmaVillageItem> list = dto.response().body().items().item().stream()
            .map(i -> new KmaVillageItem(
                i.baseDate(), i.baseTime(),
                i.fcstDate(), i.fcstTime(),
                i.category(), i.fcstValue(),
                i.nx(), i.ny()))
            .toList();
        return new KmaVillageResponse(list);
    }

    /* ---------- OpenAPI 응답 DTO ---------- */
    public record KmaOpenApiDto(Response response) {
        public record Response(Body body) {
            public record Body(Items items) {
                public record Items(List<Item> item) {
                    public record Item(String baseDate, String baseTime,
                                       String fcstDate, String fcstTime,
                                       String category, String fcstValue,
                                       int nx, int ny) { }
                }
            }
        }
    }
}
