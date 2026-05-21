package com.wanted.codebombalms.admin.operation.alert.presentation.api.response;

import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertListItem;
import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
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

    public static OperationAlertResponse from(OperationAlertListItem alert) {
        return new OperationAlertResponse(
                alert.operationAlertId(),
                alert.operationRuleId(),
                alert.targetType(),
                alert.targetId(),
                alert.detectedValue(),
                alert.thresholdValueSnapshot(),
                alert.severity(),
                alert.status(),
                alert.assigneeId(),
                alert.reason(),
                alert.recommendedAction(),
                alert.firstDetectedAt(),
                alert.lastDetectedAt()
        );
    }
}