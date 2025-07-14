package com.codeit.sb01otbooteam06.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger("KMA-WEBCLIENT");

    /** 요청 로그 */
    private static final ExchangeFilterFunction logRequest =
        ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.info("➡️  {} {}", req.method(), req.url());
            req.headers().forEach((n, v) -> log.debug("  {}: {}", n, v));
            return Mono.just(req);
        });

    /** 응답 로그 */
    private static final ExchangeFilterFunction logResponse =
        ExchangeFilterFunction.ofResponseProcessor(res -> {
            log.info("⬅️  {} {}", res.statusCode(), res.headers().contentType().orElse(null));
            return Mono.just(res);
        });

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .filter(logRequest)
            .filter(logResponse)
            .build();
    }
}

