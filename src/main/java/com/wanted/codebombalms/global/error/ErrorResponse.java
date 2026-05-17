package com.wanted.codebombalms.global.error;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ErrorResponse {

    private int status;
    private String errorCode;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    public ErrorResponse(HttpStatus status, String errorCode, String message, String path) {
        this.status = status.value();
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}