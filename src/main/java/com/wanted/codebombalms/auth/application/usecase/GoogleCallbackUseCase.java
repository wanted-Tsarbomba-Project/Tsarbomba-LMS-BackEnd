package com.wanted.codebombalms.auth.application.usecase;

import com.wanted.codebombalms.auth.application.dto.GoogleCallbackResult;

public interface GoogleCallbackUseCase {

    /** 구글 콜백 처리 — state 검증 → 토큰 교환 → 기존/신규 분기 */
    GoogleCallbackResult handleCallback(String code, String state);
}
