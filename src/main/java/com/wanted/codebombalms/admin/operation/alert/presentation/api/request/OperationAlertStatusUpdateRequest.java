package com.wanted.codebombalms.admin.operation.alert.presentation.api.request;

import com.wanted.codebombalms.admin.operation.alert.application.command.UpdateOperationAlertStatusCommand;
import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OperationAlertStatusUpdateRequest {

    private OperationAlertStatus status;

    public UpdateOperationAlertStatusCommand toCommand(Long operationAlertId, Long resolvedBy) {
        return new UpdateOperationAlertStatusCommand(
                operationAlertId,
                status,
                resolvedBy
        );
    }
}
