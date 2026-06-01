package com.wanted.codebombalms.global.presentation.api.common;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;

import java.time.Instant;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path
) {
    public static ApiErrorResponse of(int status, ErrorCode errorCode, String path) {
        return new ApiErrorResponse(Instant.now(), status, errorCode.getCode(), errorCode.getMessage(), path);
    }

    public static ApiErrorResponse of(int status, String code, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, code, message, path);
    }
} 