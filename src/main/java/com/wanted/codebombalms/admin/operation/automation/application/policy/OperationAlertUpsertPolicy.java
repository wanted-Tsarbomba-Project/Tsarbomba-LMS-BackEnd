package com.wanted.codebombalms.admin.operation.automation.application.policy;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;
import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
// 탐지 결과를 기존 OPEN 알림 갱신 또는 신규 알림 생성으로 결정한다.
public class OperationAlertUpsertPolicy {

    public OperationAlert resolve(
            AutomationRule rule,
            OperationRuleDetectionResult result,
            Optional<OperationAlert> existingAlert,
            LocalDateTime detectedAt
    ) {
        return existingAlert
                .map(alert -> update(alert, result, detectedAt))
                .orElseGet(() -> create(rule, result, detectedAt));
    }

    private OperationAlert update(
            OperationAlert alert,
            OperationRuleDetectionResult result,
            LocalDateTime detectedAt
    ) {
        alert.updateDetectedValue(
                result.detectedValue(),
                result.reason(),
                result.recommendedAction(),
                detectedAt
        );

        return alert;
    }

    private OperationAlert create(
            AutomationRule rule,
            OperationRuleDetectionResult result,
            LocalDateTime detectedAt
    ) {
        return OperationAlert.create(
                rule.getOperationRuleId(),
                result.targetType(),
                result.targetId(),
                result.detectedValue(),
                rule.getThresholdValue(),
                null,
                result.reason(),
                result.recommendedAction(),
                detectedAt
        );
    }
}
