package com.wanted.codebombalms.auth.application.usecase;

import com.wanted.codebombalms.auth.application.dto.TokenPair;

public interface CompleteSocialSignupUseCase {

    TokenPair complete(String tempToken, String nickname, String phone);
}
