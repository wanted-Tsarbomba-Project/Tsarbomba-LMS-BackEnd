package com.wanted.codebombalms.global.error;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private final int status;
    private final String errorCode;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    // BusinessException용
    public ErrorResponse(ErrorCode errorCode, String path) {
        this.status = errorCode.getStatus();
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    // Validation / 500용
    public ErrorResponse(int status, String errorCode, String message, String path) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}