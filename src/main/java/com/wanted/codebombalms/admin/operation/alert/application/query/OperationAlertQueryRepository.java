package com.wanted.codebombalms.admin.operation.alert.application.query;

import com.wanted.codebombalms.admin.operation.common.application.PageResult;

public interface OperationAlertQueryRepository {

    PageResult<OperationAlertListItem> findAlerts(GetOperationAlertsQuery query);
}
