package com.wanted.codebombalms.domain.admin.operation.alert.infrastructure.persistence;

import com.wanted.codebombalms.domain.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operation_alert")
@Getter
@NoArgsConstructor
public class OperationAlertJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_alert_id")
    private Long operationAlertId;

    @Column(name = "operation_rule_id", nullable = false)
    private Long operationRuleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private OperationTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "detected_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal detectedValue;

    @Column(name = "threshold_value_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal thresholdValueSnapshot;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "recommended_action", length = 500)
    private String recommendedAction;

    @Column(name = "first_detected_at", nullable = false)
    private LocalDateTime firstDetectedAt;

    @Column(name = "last_detected_at", nullable = false)
    private LocalDateTime lastDetectedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OperationAlertStatus status;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "admin_memo", length = 500)
    private String adminMemo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public OperationAlertJpaEntity(
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

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;

        if (this.firstDetectedAt == null) {
            this.firstDetectedAt = now;
        }

        if (this.lastDetectedAt == null) {
            this.lastDetectedAt = now;
        }

        if (this.status == null) {
            this.status = OperationAlertStatus.OPEN;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}