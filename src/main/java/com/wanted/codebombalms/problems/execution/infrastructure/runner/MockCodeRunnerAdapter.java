package com.wanted.codebombalms.problems.execution.infrastructure.runner;

import org.springframework.beans.factory.annotation.Value;
import com.wanted.codebombalms.problems.execution.application.port.RunCodePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "code-runner.type", havingValue = "mock", matchIfMissing = true)
public class MockCodeRunnerAdapter implements RunCodePort {

    private final long mockDelayMs;

    public MockCodeRunnerAdapter(
            @Value("${code-runner.mock.delay-ms:300}") long mockDelayMs
    ) {
        if (mockDelayMs < 0) {
            throw new IllegalArgumentException("code-runner.mock.delay-ms는 0 이상이어야 합니다.");
        }
        this.mockDelayMs = mockDelayMs;
    }

    @Override
    public CodeRunResult run(CodeRunCommand command) {
        waitForExternalExecution();
        String code = command.code();


        if (code.contains("raise") || code.contains("error")) {
            return new CodeRunResult(
                    null,
                    "코드 실행 중 오류가 발생했습니다.",
                    mockDelayMs,
                    false
            );
        }

        if (!code.contains("result")) {
            return new CodeRunResult(
                    null,
                    "result 변수가 정의되지 않았습니다.",
                    mockDelayMs,
                    false
            );
        }

        return new CodeRunResult(
                "실행 결과를 확인했습니다.",
                null,
                mockDelayMs,
                true
        );
    }

    private void waitForExternalExecution() {
        try {
            Thread.sleep(mockDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Mock 코드 실행 대기가 중단되었습니다.", e);
        }
    }
}
