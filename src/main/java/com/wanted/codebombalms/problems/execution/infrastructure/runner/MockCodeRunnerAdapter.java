package com.wanted.codebombalms.problems.execution.infrastructure.runner;

import com.wanted.codebombalms.problems.execution.application.port.RunCodePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "code-runner.type", havingValue = "mock", matchIfMissing = true)
public class MockCodeRunnerAdapter implements RunCodePort {

    private static final long MOCK_EXECUTION_TIME_MS = 1L;

    @Override
    public CodeRunResult run(CodeRunCommand command) {
        String code = command.code();

        if (code.contains("raise") || code.contains("error")) {
            return new CodeRunResult(
                    null,
                    "코드 실행 중 오류가 발생했습니다.",
                    MOCK_EXECUTION_TIME_MS,
                    false
            );
        }

        if (!code.contains("result")) {
            return new CodeRunResult(
                    null,
                    "result 변수가 정의되지 않았습니다.",
                    MOCK_EXECUTION_TIME_MS,
                    false
            );
        }

        return new CodeRunResult(
                "실행 결과를 확인했습니다.",
                null,
                MOCK_EXECUTION_TIME_MS,
                true
        );
    }
}
