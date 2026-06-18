package com.wanted.codebombalms.problems.execution.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Code Execution 도메인의 외부 Runner 호출 구간을 측정하는 메트릭 컴포넌트.
 */
@Component
public class CodeExecutionMetrics {

    private final Timer runnerExecutionTimer;

    public CodeExecutionMetrics(MeterRegistry registry) {
        this.runnerExecutionTimer = Timer.builder("code_execution_runner_duration")
                .description("Python Runner를 호출해 사용자 코드를 실행하는 데 걸린 시간")
                .register(registry);
    }

    public void recordRunnerExecution(long elapsedNanos) {
        runnerExecutionTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
}
