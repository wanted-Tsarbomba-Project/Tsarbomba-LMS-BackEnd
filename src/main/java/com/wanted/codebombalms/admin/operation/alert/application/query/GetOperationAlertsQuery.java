package com.wanted.codebombalms.admin.operation.alert.application.query;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;

public record GetOperationAlertsQuery(
        OperationTargetType targetType,
        OperationAlertStatus status,
        int page,
        int size
) {
}