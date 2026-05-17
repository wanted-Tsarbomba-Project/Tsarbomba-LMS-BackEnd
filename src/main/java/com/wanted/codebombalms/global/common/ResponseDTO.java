package com.wanted.codebombalms.global.common;

import lombok.*;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ResponseDTO {

    private int status;
    private String message;
    private Object data;

    public ResponseDTO(HttpStatus status, String message, Object data) {
        this.status = status.value();
        this.message = message;
        this.data = data;
    }
}