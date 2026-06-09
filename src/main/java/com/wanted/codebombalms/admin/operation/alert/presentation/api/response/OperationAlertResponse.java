package com.wanted.codebombalms.admin.operation.alert.presentation.api.response;

import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertListItem;
import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;

public record OperationAlertResponse(
        Long operationAlertId,
        OperationAlertStatus status,
        String recommendedAction
) {

    public static OperationAlertResponse from(OperationAlertListItem alert) {
        return new OperationAlertResponse(
                alert.operationAlertId(),
                alert.status(),
                alert.recommendedAction()
        );
    }
}
