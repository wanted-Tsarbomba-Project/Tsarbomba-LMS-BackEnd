package com.wanted.codebombalms.global.presentation.api.common;

import java.time.Instant;

// 공통 에러 응답 포멧
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message
) {

    public static ApiErrorResponse of(int status, String code, String message) {
        return new ApiErrorResponse(Instant.now(), status, code, message);
    }
}
