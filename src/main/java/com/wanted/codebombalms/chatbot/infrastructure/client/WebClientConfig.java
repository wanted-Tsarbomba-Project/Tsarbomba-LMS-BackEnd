package com.wanted.codebombalms.chatbot.infrastructure.client;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final FastApiProperties fastApiProperties;

    @Bean
    public WebClient webClient() {
        // 스트리밍 응답은 길게 이어지므로 30초 → 100초로 상한을 늘린다(무제한은 비정상 연결 점유 위험).
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                .responseTimeout(Duration.ofSeconds(100));

        return WebClient.builder()
                .baseUrl(fastApiProperties.getUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}