package com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;

public record OperationAlertListProjection(
        Long operationAlertId,
        OperationAlertStatus status,
        String recommendedAction
) {
}
