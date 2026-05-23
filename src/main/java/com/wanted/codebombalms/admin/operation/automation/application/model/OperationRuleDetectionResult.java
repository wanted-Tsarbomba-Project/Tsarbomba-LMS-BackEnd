package com.wanted.codebombalms.admin.operation.automation.application.model;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;

import java.math.BigDecimal;

// 운영 규칙 실행으로 탐지된 알림 후보 정보를 담는다.
public record OperationRuleDetectionResult(
        OperationTargetType targetType,
        Long targetId,
        BigDecimal detectedValue,
        String reason,
        String recommendedAction
) {
}
