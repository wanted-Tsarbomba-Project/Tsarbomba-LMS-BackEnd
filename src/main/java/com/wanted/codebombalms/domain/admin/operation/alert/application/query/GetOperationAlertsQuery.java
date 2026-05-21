package com.wanted.codebombalms.domain.admin.operation.alert.application.query;

import com.wanted.codebombalms.domain.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;

public record GetOperationAlertsQuery(
        OperationTargetType targetType,
        OperationAlertStatus status,
        int page,
        int size
) {
}