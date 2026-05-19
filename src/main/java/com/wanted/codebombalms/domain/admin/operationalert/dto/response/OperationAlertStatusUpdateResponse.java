package com.wanted.codebombalms.domain.admin.operationalert.dto.response;

import com.wanted.codebombalms.domain.admin.operationalert.entity.OperationAlert;
import com.wanted.codebombalms.domain.admin.operationalert.enums.OperationAlertStatus;
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
    private String adminMemo;
    private Long resolvedBy;
    private LocalDateTime resolvedAt;

    public static OperationAlertStatusUpdateResponse from(OperationAlert operationAlert) {
        return new OperationAlertStatusUpdateResponse(
                operationAlert.getOperationAlertId(),
                operationAlert.getStatus(),
                operationAlert.getAdminMemo(),
                operationAlert.getResolvedBy(),
                operationAlert.getResolvedAt()
        );
    }
}