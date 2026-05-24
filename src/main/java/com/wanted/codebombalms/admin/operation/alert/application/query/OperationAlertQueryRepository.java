package com.wanted.codebombalms.admin.operation.alert.application.query;

import com.wanted.codebombalms.admin.operation.common.application.PageResult;

import java.util.Optional;

public interface OperationAlertQueryRepository {

    PageResult<OperationAlertListItem> findAlerts(GetOperationAlertsQuery query);

    Optional<OperationAlertDetail> findAlertDetail(Long operationAlertId);
}
