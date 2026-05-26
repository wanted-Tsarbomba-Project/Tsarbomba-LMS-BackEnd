package com.wanted.codebombalms.problems.execution.infrastructure.runner;

import com.wanted.codebombalms.problems.execution.application.port.RunCodePort;
import com.wanted.codebombalms.problems.execution.exception.ProblemCodeExecutionException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Primary
@Component
public class PythonCodeRunnerAdapter implements RunCodePort {

    private final CodeRunnerProperties properties;

    public PythonCodeRunnerAdapter(CodeRunnerProperties properties) {
        this.properties = properties;
    }

    @Override
    public CodeRunResult run(String code) {
        Path tempFile = null;
        long startTime = System.currentTimeMillis();

        try {
            tempFile = Files.createTempFile("codebomba-", ".py");

            String executableCode = appendResultPrinter(code);
            Files.writeString(tempFile, executableCode, StandardCharsets.UTF_8);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    properties.getPythonCommand(),
                    tempFile.toAbsolutePath().toString()
            );
            processBuilder.directory(Path.of("").toAbsolutePath().toFile());
            processBuilder.environment().put("PYTHONUTF8", "1");

            Process process = processBuilder.start();

            boolean finished = process.waitFor(
                    properties.getTimeoutSeconds(),
                    TimeUnit.SECONDS
            );

            long executionTimeMs = System.currentTimeMillis() - startTime;

            if (!finished) {
                process.destroyForcibly();

                return new CodeRunResult(
                        null,
                        "코드 실행 시간이 초과되었습니다.",
                        executionTimeMs,
                        false
                );
            }

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            ).trim();

            String error = new String(
                    process.getErrorStream().readAllBytes(),
                    StandardCharsets.UTF_8
            ).trim();

            if (process.exitValue() != 0) {
                return new CodeRunResult(
                        null,
                        error,
                        executionTimeMs,
                        false
                );
            }

            return new CodeRunResult(
                    output,
                    null,
                    executionTimeMs,
                    true
            );

        } catch (IOException e) {
            throw new ProblemCodeExecutionException();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProblemCodeExecutionException();
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private String appendResultPrinter(String code) {
        return code + System.lineSeparator()
                + System.lineSeparator()
                + "print(result)";
    }
}
