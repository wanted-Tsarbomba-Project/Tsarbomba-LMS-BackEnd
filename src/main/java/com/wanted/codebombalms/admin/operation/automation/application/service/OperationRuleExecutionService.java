package com.wanted.codebombalms.admin.operation.automation.application.service;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;
import com.wanted.codebombalms.admin.operation.alert.domain.repository.OperationAlertRepository;
import com.wanted.codebombalms.admin.operation.automation.application.handler.OperationRuleHandler;
import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.automation.application.policy.OperationAlertUpsertPolicy;
import com.wanted.codebombalms.admin.operation.automation.application.policy.OperationRuleExecutionPolicy;
import com.wanted.codebombalms.admin.operation.automation.application.usecase.RunOperationRuleUseCase;
import com.wanted.codebombalms.admin.operation.metrics.AdminMetrics;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import com.wanted.codebombalms.admin.operation.rule.domain.repository.AutomationRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
// 활성화된 자동 규칙을 실행하고 탐지 결과를 운영 알림으로 반영한다.
public class OperationRuleExecutionService implements RunOperationRuleUseCase {

    private final AutomationRuleRepository automationRuleRepository;
    private final OperationAlertRepository operationAlertRepository;
    private final Map<OperationRuleCode, OperationRuleHandler> handlers;
    private final OperationRuleExecutionPolicy operationRuleExecutionPolicy;
    private final OperationAlertUpsertPolicy operationAlertUpsertPolicy;
    private final Clock clock;
    private final AdminMetrics adminMetrics;

    public OperationRuleExecutionService(
            AutomationRuleRepository automationRuleRepository,
            OperationAlertRepository operationAlertRepository,
            List<OperationRuleHandler> handlers,
            OperationRuleExecutionPolicy operationRuleExecutionPolicy,
            OperationAlertUpsertPolicy operationAlertUpsertPolicy,
            Clock clock,
            AdminMetrics adminMetrics
    ) {
        this.automationRuleRepository = automationRuleRepository;
        this.operationAlertRepository = operationAlertRepository;
        this.operationRuleExecutionPolicy = operationRuleExecutionPolicy;
        this.operationAlertUpsertPolicy = operationAlertUpsertPolicy;
        this.clock = clock;
        this.adminMetrics = adminMetrics;
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(
                        OperationRuleHandler::supports,
                        Function.identity(),
                        (left, right) -> left,
                        () -> new EnumMap<>(OperationRuleCode.class)
                ));
    }

    @Override
    public void run() {
        long startedAt = System.nanoTime();
        List<AutomationRule> enabledRules = automationRuleRepository.findEnabled();
        int detectedCount = enabledRules.stream()
                .mapToInt(this::executeRule)
                .sum();
        long elapsedNanos = System.nanoTime() - startedAt;

        adminMetrics.recordRuleRun(elapsedNanos);
        log.info("event=admin_operation_rule_run_completed enabledRuleCount={} detectedCount={} durationMs={}",
                enabledRules.size(), detectedCount, elapsedNanos / 1_000_000);
    }

    private int executeRule(AutomationRule rule) {
        OperationRuleHandler handler = handlers.get(rule.getRuleCode());
        if (!operationRuleExecutionPolicy.canExecute(rule, handler)) {
            return 0;
        }

        long startedAt = System.nanoTime();
        List<OperationRuleDetectionResult> results = handler.detect(rule);
        long elapsedNanos = System.nanoTime() - startedAt;

        adminMetrics.recordRuleDetect(rule.getRuleCode(), elapsedNanos);
        adminMetrics.incrementRuleDetected(rule.getRuleCode(), results.size());
        log.info("event=admin_operation_rule_detected ruleCode={} detectedCount={} durationMs={}",
                rule.getRuleCode(), results.size(), elapsedNanos / 1_000_000);

        results.forEach(result -> saveAlert(rule, result));
        return results.size();
    }

    private void saveAlert(AutomationRule rule, OperationRuleDetectionResult result) {
        long startedAt = System.nanoTime();
        LocalDateTime detectedAt = LocalDateTime.now(clock);

        OperationAlert operationAlert = operationAlertUpsertPolicy.resolve(
                rule,
                result,
                operationAlertRepository.findOpenByRuleIdAndTarget(
                        rule.getOperationRuleId(),
                        result.targetType(),
                        result.targetId()
                ),
                detectedAt
        );

        operationAlertRepository.save(operationAlert);
        long elapsedNanos = System.nanoTime() - startedAt;

        adminMetrics.recordAlertUpsert(elapsedNanos);
        log.info("event=admin_operation_alert_upserted ruleCode={} targetType={} alertCount=1 durationMs={}",
                rule.getRuleCode(), result.targetType(), elapsedNanos / 1_000_000);
    }
}
