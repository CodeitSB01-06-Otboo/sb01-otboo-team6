package com.codeit.sb01otbooteam06.domain.weather.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.net.HttpHeaders;
import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
//
//@ExtendWith(MockitoExtension.class)
//class KakaoLocalClientTest {
//
//    MockWebServer server;
//    KakaoLocalClient client;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        server = new MockWebServer();
//        server.start();
//
//        // WebClient 가 MockWebServer 로 향하도록 baseUrl 교체
//        WebClient wc = WebClient.builder()
//            .baseUrl(server.url("/").toString())
//            .build();
//
//        // (1) 생성자는 WebClient 하나만
//        client = new KakaoLocalClient(wc);
//
//        // (2) @Value 필드 주입 대체
//        ReflectionTestUtils.setField(client, "kakaoKey", "dummy-rest-key");
//    }
//
//    @AfterEach
//    void tearDown() throws IOException {
//        server.shutdown();
//    }
//
//    @Test
//    void coordToRegion_success() throws Exception {
//        // ── stub JSON ──
//        String body = """
//        {
//          "documents": [
//            { "region_type":"B",
//              "address_name":"서울 강남구 역삼동",
//              "region_1depth_name":"서울특별시",
//              "region_2depth_name":"강남구",
//              "region_3depth_name":"역삼동",
//              "region_4depth_name":"" }
//          ]
//        }""";
//        server.enqueue(new MockResponse()
//            .setResponseCode(200)
//            .setHeader("Content-Type","application/json")
//            .setBody(body));
//
//        // ── when ──
//        List<String> names = client.coordToRegion(37, 127);
//
//        // ── then ──
//        assertThat(names).containsExactly("서울특별시","강남구","역삼동","");
//        // 요청 검증
//        RecordedRequest rq = server.takeRequest();
//        assertThat(rq.getHeader("Authorization"))
//            .isEqualTo("KakaoAK dummy-rest-key");
//    }
//}
