package com.wanted.codebombalms.global.domain.common.error;

public interface ErrorCode {
    int getStatus();
    String getCode();
    String getMessage();
}