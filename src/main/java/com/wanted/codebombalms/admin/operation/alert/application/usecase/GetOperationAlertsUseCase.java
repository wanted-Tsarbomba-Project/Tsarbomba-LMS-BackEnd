package com.wanted.codebombalms.admin.operation.alert.application.usecase;

import com.wanted.codebombalms.admin.operation.alert.application.query.GetOperationAlertsQuery;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertListItem;
import com.wanted.codebombalms.admin.operation.common.application.PageResult;

public interface GetOperationAlertsUseCase {

    PageResult<OperationAlertListItem> getAlerts(GetOperationAlertsQuery query);
}