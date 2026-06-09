package com.wanted.codebombalms.admin.operation.alert.application.query;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
public record OperationAlertListItem(
        Long operationAlertId,
        OperationAlertStatus status,
        String recommendedAction
) {
}
