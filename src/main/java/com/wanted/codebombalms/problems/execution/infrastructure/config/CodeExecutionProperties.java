package com.wanted.codebombalms.problems.execution.infrastructure.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "code-execution")
public class CodeExecutionProperties {


    public int getDefaultTimeoutMs() {
        return defaultTimeoutMs;
    }

    public void setDefaultTimeoutMs(int defaultTimeoutMs) {
        this.defaultTimeoutMs = defaultTimeoutMs;
    }
    @Min(value = 100, message = "기본 실행 제한 시간은 100ms 이상이어야 합니다.")
    @Max(value = 10000, message = "기본 실행 제한 시간은 10000ms 이하여야 합니다.")
    private int defaultTimeoutMs = 5000;
}
