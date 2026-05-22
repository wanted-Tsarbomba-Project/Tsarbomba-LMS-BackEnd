package com.wanted.codebombalms.admin.operation.rule.domain.model;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.exception.AutomationRuleErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AutomationRule {

    private final Long operationRuleId;
    private final Long createdBy;
    private final OperationRuleCode ruleCode;
    private final BigDecimal thresholdValue;
    private final Integer minSampleCount;
    private final OperationSeverity severity;
    private final boolean enabled;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private AutomationRule(
            Long operationRuleId,
            Long createdBy,
            OperationRuleCode ruleCode,
            BigDecimal thresholdValue,
            Integer minSampleCount,
            OperationSeverity severity,
            boolean enabled,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.operationRuleId = operationRuleId;
        this.createdBy = createdBy;
        this.ruleCode = ruleCode;
        this.thresholdValue = thresholdValue;
        this.minSampleCount = minSampleCount;
        this.severity = severity;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AutomationRule create(
            Long createdBy,
            OperationRuleCode ruleCode,
            BigDecimal thresholdValue,
            Integer minSampleCount,
            OperationSeverity severity,
            Boolean enabled
    ) {
        validateCreateRequest(createdBy, ruleCode, thresholdValue, minSampleCount);

        OperationSeverity resolvedSeverity = severity == null ? OperationSeverity.MEDIUM : severity;
        boolean resolvedEnabled = enabled == null || enabled;

        return new AutomationRule(
                null,
                createdBy,
                ruleCode,
                thresholdValue,
                normalizeMinSampleCount(ruleCode, minSampleCount),
                resolvedSeverity,
                resolvedEnabled,
                null,
                null
        );
    }

    public static AutomationRule restore(
            Long operationRuleId,
            Long createdBy,
            OperationRuleCode ruleCode,
            BigDecimal thresholdValue,
            Integer minSampleCount,
            OperationSeverity severity,
            boolean enabled,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new AutomationRule(
                operationRuleId,
                createdBy,
                ruleCode,
                thresholdValue,
                minSampleCount,
                severity,
                enabled,
                createdAt,
                updatedAt
        );
    }


    // threshold 범위 검증, 각 도메인별 minSampleCount 규칙 검증
    public AutomationRule updateThreshold(
            BigDecimal thresholdValue,
            Integer minSampleCount
    ) {
        validateThresholdRequest(ruleCode, thresholdValue, minSampleCount);

        return new AutomationRule(
                operationRuleId,
                createdBy,
                ruleCode,
                thresholdValue,
                normalizeMinSampleCount(ruleCode, minSampleCount),
                severity,
                enabled,
                createdAt,
                updatedAt
        );
    }

    // enable null 검증
    public AutomationRule updateEnabled(Boolean enabled) {
        if (enabled == null) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_ENABLED_UPDATE_REQUEST);
        }

        return new AutomationRule(
                operationRuleId,
                createdBy,
                ruleCode,
                thresholdValue,
                minSampleCount,
                severity,
                enabled,
                createdAt,
                updatedAt
        );
    }

    private static Integer normalizeMinSampleCount(OperationRuleCode ruleCode, Integer minSampleCount) {
        if (!ruleCode.isRequiresMinSampleCount()) {
            return null;
        }

        return minSampleCount;
    }

    private static void validateCreateRequest(
            Long createdBy,
            OperationRuleCode ruleCode,
            BigDecimal thresholdValue,
            Integer minSampleCount
    ) {
        if (createdBy == null || ruleCode == null || thresholdValue == null) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_CREATE_REQUEST);
        }

        validateThresholdRequest(ruleCode, thresholdValue, minSampleCount);
    }

    private static void validateThresholdRequest(
            OperationRuleCode ruleCode,
            BigDecimal thresholdValue,
            Integer minSampleCount
    ) {
        if (ruleCode == null || thresholdValue == null) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_UPDATE_REQUEST);
        }

        if (!ruleCode.isValidThreshold(thresholdValue)) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_THRESHOLD_VALUE);
        }

        if (ruleCode.isRequiresMinSampleCount()
                && (minSampleCount == null || minSampleCount <= 0)) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_MIN_SAMPLE_COUNT);
        }
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
        return ruleCode.getLabel();
    }

    public OperationTargetType getTargetType() {
        return ruleCode.getTargetType();
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

}
