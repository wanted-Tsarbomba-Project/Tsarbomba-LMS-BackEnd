package com.wanted.codebombalms.domain.admin.operationalert.dto.response;

import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationSeverity;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationTargetType;
import com.wanted.codebombalms.domain.admin.operationalert.entity.OperationAlert;
import com.wanted.codebombalms.domain.admin.operationalert.enums.OperationAlertStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OperationAlertResponse {

    private Long operationAlertId;
    private Long operationRuleId;
    private OperationTargetType targetType;
    private Long targetId;
    private BigDecimal detectedValue;
    private BigDecimal thresholdValueSnapshot;
    private OperationSeverity severity;
    private OperationAlertStatus status;
    private Long assigneeId;
    private String reason;
    private String recommendedAction;
    private LocalDateTime firstDetectedAt;
    private LocalDateTime lastDetectedAt;

    public static OperationAlertResponse from(OperationAlert alert) {
        return new OperationAlertResponse(
                alert.getOperationAlertId(),
                alert.getAutomationRule().getOperationRuleId(),
                alert.getTargetType(),
                alert.getTargetId(),
                alert.getDetectedValue(),
                alert.getThresholdValueSnapshot(),
                alert.getAutomationRule().getSeverity(),
                alert.getStatus(),
                alert.getAssigneeId(),
                alert.getReason(),
                alert.getRecommendedAction(),
                alert.getFirstDetectedAt(),
                alert.getLastDetectedAt()
        );
    }
}