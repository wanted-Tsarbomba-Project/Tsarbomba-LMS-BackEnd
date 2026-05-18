package com.wanted.codebombalms.domain.admin.operationalert.entity;

import com.wanted.codebombalms.domain.admin.automationrule.entity.AutomationRule;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationTargetType;
import com.wanted.codebombalms.domain.admin.operationalert.enums.OperationAlertStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operation_alert")
@Getter
@NoArgsConstructor
public class OperationAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_alert_id")
    private Long operationAlertId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_rule_id", nullable = false)
    private AutomationRule automationRule;

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

    public void process(
            OperationAlertStatus status,
            Long resolvedBy,
            String adminMemo
    ) {
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
    }


}
