package com.wanted.codebombalms.domain.admin.operation.alert.presentation.api.request;

import com.wanted.codebombalms.domain.admin.operation.alert.application.command.UpdateOperationAlertStatusCommand;
import com.wanted.codebombalms.domain.admin.operation.alert.domain.model.OperationAlertStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OperationAlertStatusUpdateRequest {

    private OperationAlertStatus status;
    private String adminMemo;
    private Long resolvedBy;

    public UpdateOperationAlertStatusCommand toCommand(Long operationAlertId) {
        return new UpdateOperationAlertStatusCommand(
                operationAlertId,
                status,
                adminMemo,
                resolvedBy
        );
    }
}