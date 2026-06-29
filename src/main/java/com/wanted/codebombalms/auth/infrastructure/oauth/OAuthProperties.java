package com.wanted.codebombalms.auth.infrastructure.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    private Google google = new Google();
    private String additionalInfoUri;  // 신규 회원 → 추가정보 입력 페이지
    private String successUri;         // 기존 회원 로그인 성공 → 메인
    private String errorUri;           // OAuth 실패 → 프론트 로그인 페이지(모달 표시)

    @Getter
    @Setter
    public static class Google {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String authorizationUri;
        private String tokenUri;
        private String userInfoUri;
        private List<String> scope;
    }
}
