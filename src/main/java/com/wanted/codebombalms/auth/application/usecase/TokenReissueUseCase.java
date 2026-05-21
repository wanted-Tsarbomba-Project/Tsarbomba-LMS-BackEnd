package com.wanted.codebombalms.auth.application.usecase;

import com.wanted.codebombalms.auth.application.dto.TokenPair;

public interface TokenReissueUseCase {

    TokenPair reissue(String refreshToken);
}