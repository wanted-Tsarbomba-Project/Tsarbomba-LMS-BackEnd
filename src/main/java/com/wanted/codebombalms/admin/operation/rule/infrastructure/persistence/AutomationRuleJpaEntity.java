package com.wanted.codebombalms.admin.operation.rule.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "automation_rule",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_automation_rule_code", columnNames = "rule_code")
        }
)
@Getter
@NoArgsConstructor
public class AutomationRuleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_rule_id")
    private Long operationRuleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_code", nullable = false)
    private OperationRuleCode ruleCode;

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

    public AutomationRuleJpaEntity(
            Long operationRuleId,
            OperationRuleCode ruleCode,
            BigDecimal thresholdValue,
            Integer minSampleCount,
            OperationSeverity severity,
            boolean enabled,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.operationRuleId = operationRuleId;
        this.ruleCode = ruleCode;
        this.thresholdValue = thresholdValue;
        this.minSampleCount = minSampleCount;
        this.severity = severity;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
