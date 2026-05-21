package com.wanted.codebombalms.admin.operation.rule.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "automation_rule")
@Getter
@NoArgsConstructor
public class AutomationRuleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_rule_id")
    private Long operationRuleId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_code", nullable = false)
    private OperationRuleCode ruleCode;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private OperationTargetType targetType;

    @Column(name = "threshold_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal thresholdValue;

    @Column(name = "min_sample_count")
    private Integer minSampleCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private OperationSeverity severity;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public AutomationRuleJpaEntity(
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

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}