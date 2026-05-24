package com.wanted.codebombalms.admin.operation.alert.application.query;

import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;

// 운영 알림 상세 조회에 표시할 자동 규칙 설명 정보를 담는다.
public record OperationAlertRuleInfo(
        OperationRuleCode ruleCode,
        String ruleName,
        String description,
        String thresholdLabel,
        String thresholdUnit,
        Integer minSampleCount,
        String minSampleCountLabel,
        String minSampleCountUnit
) {

    // 규칙 코드의 메타데이터와 저장된 최소 표본 수를 조회 결과로 변환한다.
    public static OperationAlertRuleInfo from(OperationRuleCode ruleCode, Integer minSampleCount) {
        return new OperationAlertRuleInfo(
                ruleCode,
                ruleCode.getLabel(),
                ruleCode.getDescription(),
                ruleCode.getThresholdLabel(),
                ruleCode.getThresholdUnit(),
                minSampleCount,
                ruleCode.getMinSampleCountLabel(),
                ruleCode.getMinSampleCountUnit()
        );
    }
}
