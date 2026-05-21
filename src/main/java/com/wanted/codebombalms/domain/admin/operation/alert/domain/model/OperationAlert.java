package com.wanted.codebombalms.domain.admin.operation.alert.domain.model;

import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OperationAlert {

    private final Long operationAlertId;
    private final Long operationRuleId;
    private final OperationTargetType targetType;
    private final Long targetId;
    private BigDecimal detectedValue;
    private final BigDecimal thresholdValueSnapshot;
    private final Long assigneeId;
    private String reason;
    private String recommendedAction;
    private final LocalDateTime firstDetectedAt;
    private LocalDateTime lastDetectedAt;
    private OperationAlertStatus status;
    private Long resolvedBy;
    private LocalDateTime resolvedAt;
    private String adminMemo;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private OperationAlert(
            Long operationAlertId,
            Long operationRuleId,
            OperationTargetType targetType,
            Long targetId,
            BigDecimal detectedValue,
            BigDecimal thresholdValueSnapshot,
            Long assigneeId,
            String reason,
            String recommendedAction,
            LocalDateTime firstDetectedAt,
            LocalDateTime lastDetectedAt,
            OperationAlertStatus status,
            Long resolvedBy,
            LocalDateTime resolvedAt,
            String adminMemo,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.operationAlertId = operationAlertId;
        this.operationRuleId = operationRuleId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.detectedValue = detectedValue;
        this.thresholdValueSnapshot = thresholdValueSnapshot;
        this.assigneeId = assigneeId;
        this.reason = reason;
        this.recommendedAction = recommendedAction;
        this.firstDetectedAt = firstDetectedAt;
        this.lastDetectedAt = lastDetectedAt;
        this.status = status;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
        this.adminMemo = adminMemo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OperationAlert restore(
            Long operationAlertId,
            Long operationRuleId,
            OperationTargetType targetType,
            Long targetId,
            BigDecimal detectedValue,
            BigDecimal thresholdValueSnapshot,
            Long assigneeId,
            String reason,
            String recommendedAction,
            LocalDateTime firstDetectedAt,
            LocalDateTime lastDetectedAt,
            OperationAlertStatus status,
            Long resolvedBy,
            LocalDateTime resolvedAt,
            String adminMemo,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new OperationAlert(
                operationAlertId,
                operationRuleId,
                targetType,
                targetId,
                detectedValue,
                thresholdValueSnapshot,
                assigneeId,
                reason,
                recommendedAction,
                firstDetectedAt,
                lastDetectedAt,
                status,
                resolvedBy,
                resolvedAt,
                adminMemo,
                createdAt,
                updatedAt
        );
    }

    public void process(OperationAlertStatus status, Long resolvedBy, String adminMemo) {
        if (this.status != OperationAlertStatus.OPEN) {
            throw new IllegalStateException("이미 처리된 운영 알림입니다.");
        }

        if (status != OperationAlertStatus.RESOLVED && status != OperationAlertStatus.IGNORED) {
            throw new IllegalArgumentException("운영 알림 처리 상태가 올바르지 않습니다.");
        }

        this.status = status;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = LocalDateTime.now();
        this.adminMemo = adminMemo;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDetectedValue(BigDecimal detectedValue, String reason, String recommendedAction) {
        this.detectedValue = detectedValue;
        this.reason = reason;
        this.recommendedAction = recommendedAction;
        this.lastDetectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getOperationAlertId() {
        return operationAlertId;
    }

    public Long getOperationRuleId() {
        return operationRuleId;
    }

    public OperationTargetType getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public BigDecimal getDetectedValue() {
        return detectedValue;
    }

    public BigDecimal getThresholdValueSnapshot() {
        return thresholdValueSnapshot;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public String getReason() {
        return reason;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public LocalDateTime getFirstDetectedAt() {
        return firstDetectedAt;
    }

    public LocalDateTime getLastDetectedAt() {
        return lastDetectedAt;
    }

    public OperationAlertStatus getStatus() {
        return status;
    }

    public Long getResolvedBy() {
        return resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public String getAdminMemo() {
        return adminMemo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}