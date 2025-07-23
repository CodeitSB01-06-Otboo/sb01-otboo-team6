package com.codeit.sb01otbooteam06.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

  @Value("${google.api.key}")
  private String googleApiKey;

  @Bean
  public Client genAiClient() {
    return new Client();
  }
}