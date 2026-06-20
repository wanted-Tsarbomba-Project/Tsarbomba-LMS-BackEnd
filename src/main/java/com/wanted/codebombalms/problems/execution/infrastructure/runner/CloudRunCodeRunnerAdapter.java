package com.wanted.codebombalms.problems.execution.infrastructure.runner;

import com.wanted.codebombalms.problems.execution.application.port.RunCodePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ConditionalOnProperty(name = "code-runner.type", havingValue = "cloud-run")
public class CloudRunCodeRunnerAdapter implements RunCodePort {

    private final CloudRunCodeRunnerProperties properties;
    private final RestClient restClient;

    public CloudRunCodeRunnerAdapter(
            CloudRunCodeRunnerProperties properties,
            RestClient.Builder restClientBuilder
    ) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());

        this.properties = properties;
        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public CodeRunResult run(CodeRunCommand command) {
        long startTime = System.currentTimeMillis();
        long startNanos = System.nanoTime();

        try {
            CloudRunExecuteResponse response = restClient.post()
                    .uri(properties.getEndpoint())
                    .body(new CloudRunExecuteRequest(
                            appendResultPrinter(command.code()),
                            command.datasetAccessUrl(),
                            command.timeoutMs()
                    ))
                    .retrieve()
                    .body(CloudRunExecuteResponse.class);

            long executionTimeMs = resolveExecutionTime(response, startTime);

            if (response == null) {
                log.warn(
                        "event=code_execution_runner_completed success=false reason=empty_response executionTimeMs={} durationMs={}",
                        executionTimeMs,
                        elapsedMillis(startNanos)
                );

                return new CodeRunResult(
                        null,
                        "코드 실행 서버 응답이 비어 있습니다.",
                        executionTimeMs,
                        false
                );
            }

            boolean success = Boolean.TRUE.equals(response.success());

            log.info(
                    "event=code_execution_runner_completed success={} executionTimeMs={} durationMs={}",
                    success,
                    executionTimeMs,
                    elapsedMillis(startNanos)
            );

            return new CodeRunResult(
                    response.stdout(),
                    success ? null : response.stderr(),
                    executionTimeMs,
                    success
            );
        } catch (Exception e) {
            log.error(
                    "event=code_execution_runner_failed endpoint={} exceptionType={} durationMs={}",
                    properties.getEndpoint(),
                    e.getClass().getSimpleName(),
                    elapsedMillis(startNanos),
                    e
            );

            return new CodeRunResult(
                    null,
                    "코드 실행 서버 호출에 실패했습니다.",
                    System.currentTimeMillis() - startTime,
                    false
            );
        }
    }

    private long resolveExecutionTime(CloudRunExecuteResponse response, long startTime) {
        if (response != null && response.executionTimeMs() != null) {
            return response.executionTimeMs();
        }
        return System.currentTimeMillis() - startTime;
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private String appendResultPrinter(String code) {
        return code + System.lineSeparator()
                + System.lineSeparator()
                + "print(result)";
    }

    private record CloudRunExecuteRequest(
            String code,
            String datasetAccessUrl,
            Integer timeoutMs
    ) {
    }

    private record CloudRunExecuteResponse(
            String stdout,
            String stderr,
            Boolean success,
            Long executionTimeMs
    ) {
    }
}
