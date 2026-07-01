package com.wanted.codebombalms.auth.infrastructure.oauth;

import com.wanted.codebombalms.auth.application.dto.OAuthUserInfo;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoogleOAuthClient {

    private final OAuthProperties properties;

    // 전용 WebClient (다른 도메인 설정에 의존하지 않도록 자체 구성)
    // 전용 WebClient (다른 도메인 설정에 의존하지 않도록 자체 구성, 응답 타임아웃 5초)
    private final WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create().responseTimeout(Duration.ofSeconds(5))))
            .build();

    /** 구글 동의 화면으로 보낼 authorization URL 생성 */
    public String buildAuthorizationUri(String state) {
        OAuthProperties.Google g = properties.getGoogle();
        return UriComponentsBuilder.fromUriString(g.getAuthorizationUri())
                .queryParam("client_id", g.getClientId())
                .queryParam("redirect_uri", g.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ", g.getScope()))
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
    }

    /** 인가 코드(code)를 access token 으로 교환 */
    @SuppressWarnings("unchecked")
    public String exchangeCodeForAccessToken(String code) {
        OAuthProperties.Google g = properties.getGoogle();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", g.getClientId());
        form.add("client_secret", g.getClientSecret());
        form.add("redirect_uri", g.getRedirectUri());
        form.add("grant_type", "authorization_code");

        Map<String, Object> response = webClient.post()
                .uri(g.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        Mono.error(new ValidationException(AuthErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED)))
                .bodyToMono(Map.class)
                .block();

        if (response == null || response.get("access_token") == null) {
            throw new ValidationException(AuthErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED);
        }
        return (String) response.get("access_token");
    }

    /** access token 으로 구글 사용자 정보(email, name) 조회 */
    @SuppressWarnings("unchecked")
    public OAuthUserInfo fetchUserInfo(String accessToken) {
        OAuthProperties.Google g = properties.getGoogle();

        Map<String, Object> response = webClient.get()
                .uri(g.getUserInfoUri())
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        Mono.error(new ValidationException(AuthErrorCode.OAUTH_USER_INFO_FAILED)))
                .bodyToMono(Map.class)
                .block();

        if (response == null || response.get("email") == null) {
            throw new ValidationException(AuthErrorCode.OAUTH_USER_INFO_FAILED);
        }
        return new OAuthUserInfo(
                (String) response.get("email"),
                (String) response.get("name"),
                Boolean.TRUE.equals(response.get("email_verified"))
        );

    }
}
