package com.wanted.codebombalms.domain.admin.operation.alert.application.usecase;

import com.wanted.codebombalms.domain.admin.operation.alert.application.query.GetOperationAlertsQuery;
import com.wanted.codebombalms.domain.admin.operation.alert.infrastructure.persistence.OperationAlertWithRuleProjection;
import com.wanted.codebombalms.domain.admin.operation.common.application.PageResult;

public interface GetOperationAlertsUseCase {
    PageResult<OperationAlertWithRuleProjection> getAlerts(GetOperationAlertsQuery query);
}