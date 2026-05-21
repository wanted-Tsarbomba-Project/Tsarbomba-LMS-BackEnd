package com.wanted.codebombalms.admin.operation.alert.application.usecase;

import com.wanted.codebombalms.admin.operation.alert.application.command.UpdateOperationAlertStatusCommand;
import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;

public interface UpdateOperationAlertStatusUseCase {
    OperationAlert updateStatus(UpdateOperationAlertStatusCommand command);
}