package com.wanted.codebombalms.admin.operation.loadtest;

import com.wanted.codebombalms.admin.operation.automation.application.usecase.RunOperationRuleUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@Profile("loadtest")
@RequiredArgsConstructor
@RequestMapping("/internal/loadtest/admin")
public class AdminOperationLoadTestController {

    private final RunOperationRuleUseCase runOperationRuleUseCase;

    @PostMapping("/operation-rules/run")
    public ResponseEntity<Map<String, Object>> runOperationRules() {
        long startedAt = System.nanoTime();

        runOperationRuleUseCase.run();

        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("event=loadtest_admin_operation_rule_triggered durationMs={}", durationMs);

        return ResponseEntity.ok(Map.of(
                "status", "completed",
                "durationMs", durationMs
        ));
    }
}
