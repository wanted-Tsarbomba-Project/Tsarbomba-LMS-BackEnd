package com.wanted.codebombalms.admin.operation.alert.presentation.api.response;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;
import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OperationAlertStatusUpdateResponse {

    private Long operationAlertId;
    private OperationAlertStatus status;
    private Long resolvedBy;
    private LocalDateTime resolvedAt;

    public static OperationAlertStatusUpdateResponse from(OperationAlert operationAlert) {
        return new OperationAlertStatusUpdateResponse(
                operationAlert.getOperationAlertId(),
                operationAlert.getStatus(),
                operationAlert.getResolvedBy(),
                operationAlert.getResolvedAt()
        );
    }
}
