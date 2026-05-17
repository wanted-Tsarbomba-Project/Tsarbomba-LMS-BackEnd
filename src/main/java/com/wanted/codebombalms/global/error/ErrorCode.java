package com.wanted.codebombalms.global.error;

public interface ErrorCode {
    int getStatus();
    String getCode();
    String getMessage();
}