package com.wanted.codebombalms.chatbot.infrastructure.client;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final FastApiProperties fastApiProperties;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(fastApiProperties.getUrl())
                .build();
    }
}