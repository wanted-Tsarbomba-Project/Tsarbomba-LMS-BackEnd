package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.GoogleLoginStartUseCase;
import com.wanted.codebombalms.auth.domain.repository.OAuthStateRepository;
import com.wanted.codebombalms.auth.infrastructure.oauth.GoogleOAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleLoginStartService implements GoogleLoginStartUseCase {

    private final OAuthStateRepository oAuthStateRepository;
    private final GoogleOAuthClient googleOAuthClient;

    @Override
    public String createAuthorizationUri() {
        String state = UUID.randomUUID().toString();
        oAuthStateRepository.save(state);

        return googleOAuthClient.buildAuthorizationUri(state);
    }
}
