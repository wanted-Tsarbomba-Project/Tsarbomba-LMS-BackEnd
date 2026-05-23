package com.wanted.codebombalms.admin.operation.automation.application.service;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;
import com.wanted.codebombalms.admin.operation.alert.domain.repository.OperationAlertRepository;
import com.wanted.codebombalms.admin.operation.automation.application.handler.OperationRuleHandler;
import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.automation.application.policy.OperationAlertUpsertPolicy;
import com.wanted.codebombalms.admin.operation.automation.application.policy.OperationRuleExecutionPolicy;
import com.wanted.codebombalms.admin.operation.automation.application.usecase.RunOperationRuleUseCase;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import com.wanted.codebombalms.admin.operation.rule.domain.repository.AutomationRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public OperationRuleExecutionService(
            AutomationRuleRepository automationRuleRepository,
            OperationAlertRepository operationAlertRepository,
            List<OperationRuleHandler> handlers,
            OperationRuleExecutionPolicy operationRuleExecutionPolicy,
            OperationAlertUpsertPolicy operationAlertUpsertPolicy,
            Clock clock
    ) {
        this.automationRuleRepository = automationRuleRepository;
        this.operationAlertRepository = operationAlertRepository;
        this.operationRuleExecutionPolicy = operationRuleExecutionPolicy;
        this.operationAlertUpsertPolicy = operationAlertUpsertPolicy;
        this.clock = clock;
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
        automationRuleRepository.findEnabled()
                .forEach(this::executeRule);
    }

    private void executeRule(AutomationRule rule) {
        OperationRuleHandler handler = handlers.get(rule.getRuleCode());
        if (!operationRuleExecutionPolicy.canExecute(rule, handler)) {
            return;
        }

        handler.detect(rule)
                .forEach(result -> saveAlert(rule, result));
    }

    private void saveAlert(AutomationRule rule, OperationRuleDetectionResult result) {
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
    }
}
