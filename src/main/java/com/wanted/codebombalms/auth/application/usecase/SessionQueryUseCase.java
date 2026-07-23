package com.wanted.codebombalms.auth.application.usecase;

import com.wanted.codebombalms.auth.application.dto.SessionStatus;

public interface SessionQueryUseCase {

    /**
     * 세션 잔여 시간 조회.
     *
     * @param accessToken  accessToken 쿠키 값 (없으면 null)
     * @param refreshToken refreshToken 쿠키 값 (없으면 null)
     */
    SessionStatus query(String accessToken, String refreshToken);
}
