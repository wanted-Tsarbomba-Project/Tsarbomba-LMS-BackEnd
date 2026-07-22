package com.wanted.codebombalms.inquiry.infrastructure.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/** 문의 AI 분석 전용 Python FastAPI 호출 설정입니다. */
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "inquiry.python")
public class InquiryPythonProperties {

    private boolean enabled = true;

    @NotBlank
    private String analyzePath = "/internal/inquiries/analyze";

    @Positive
    private int connectTimeoutMs = 3000;

    @Positive
    private int readTimeoutMs = 10000;
}
