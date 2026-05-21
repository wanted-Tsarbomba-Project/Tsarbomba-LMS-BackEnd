package com.wanted.codebombalms.domain.admin.operation.rule.domain.model;

import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AutomationRule {

    private final Long operationRuleId;
    private final Long createdBy;
    private final OperationRuleCode ruleCode;
    private final String ruleName;
    private final OperationTargetType targetType;
    private final BigDecimal thresholdValue;
    private final Integer minSampleCount;
    private final OperationSeverity severity;
    private final boolean enabled;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    private AutomationRule(
            Long operationRuleId,
            Long createdBy,
            OperationRuleCode ruleCode,
            String ruleName,
            OperationTargetType targetType,
            BigDecimal thresholdValue,
            Integer minSampleCount,
            OperationSeverity severity,
            boolean enabled,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        this.operationRuleId = operationRuleId;
        this.createdBy = createdBy;
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.targetType = targetType;
        this.thresholdValue = thresholdValue;
        this.minSampleCount = minSampleCount;
        this.severity = severity;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static AutomationRule create(
            Long createdBy,
            OperationRuleCode ruleCode,
            BigDecimal thresholdValue,
            Integer minSampleCount,
            OperationSeverity severity,
            Boolean enabled
    ) {
        OperationSeverity resolvedSeverity = severity == null ? OperationSeverity.MEDIUM : severity;
        boolean resolvedEnabled = enabled == null || enabled;

        return new AutomationRule(
                null,
                createdBy,
                ruleCode,
                ruleCode.getLabel(),
                ruleCode.getTargetType(),
                thresholdValue,
                normalizeMinSampleCount(ruleCode, minSampleCount),
                resolvedSeverity,
                resolvedEnabled,
                null,
                null,
                null
        );
    }

    public static AutomationRule restore(
            Long operationRuleId,
            Long createdBy,
            OperationRuleCode ruleCode,
            String ruleName,
            OperationTargetType targetType,
            BigDecimal thresholdValue,
            Integer minSampleCount,
            OperationSeverity severity,
            boolean enabled,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        return new AutomationRule(
                operationRuleId,
                createdBy,
                ruleCode,
                ruleName,
                targetType,
                thresholdValue,
                minSampleCount,
                severity,
                enabled,
                createdAt,
                updatedAt,
                deletedAt
        );
    }

    private static Integer normalizeMinSampleCount(OperationRuleCode ruleCode, Integer minSampleCount) {
        if (!ruleCode.isRequiresMinSampleCount()) {
            return null;
        }

        return minSampleCount;
    }

    public String buildRuleContent() {
        return switch (ruleCode) {
            case COURSE_LOW_ENROLLMENT ->
                    "수강생 수가 " + thresholdValue.stripTrailingZeros().toPlainString() + "명 이하인 강좌를 탐지합니다.";
            case USER_INACTIVE_NO_COURSE ->
                    "마지막 로그인 후 " + thresholdValue.stripTrailingZeros().toPlainString() + "일 이상 지났고 수강 중인 강좌가 없는 학생을 탐지합니다.";
            case PROBLEM_HIGH_WRONG_RATE ->
                    "제출 수가 " + minSampleCount + "회 이상이고 오답률이 "
                            + thresholdValue.stripTrailingZeros().toPlainString() + "% 이상인 문제를 탐지합니다.";
        };
    }

    public Long getOperationRuleId() {
        return operationRuleId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public OperationRuleCode getRuleCode() {
        return ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public OperationTargetType getTargetType() {
        return targetType;
    }

    public BigDecimal getThresholdValue() {
        return thresholdValue;
    }

    public Integer getMinSampleCount() {
        return minSampleCount;
    }

    public OperationSeverity getSeverity() {
        return severity;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}