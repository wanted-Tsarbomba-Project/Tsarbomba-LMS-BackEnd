package com.wanted.codebombalms.domain.admin.operation.alert.domain.repository;

import com.wanted.codebombalms.domain.admin.operation.alert.domain.model.OperationAlert;
import com.wanted.codebombalms.domain.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.domain.admin.operation.alert.infrastructure.persistence.OperationAlertWithRuleProjection;
import com.wanted.codebombalms.domain.admin.operation.common.application.PageResult;
import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;

import java.util.Optional;

public interface OperationAlertRepository {

    Optional<OperationAlert> findById(Long operationAlertId);

    OperationAlert save(OperationAlert operationAlert);

    PageResult<OperationAlertWithRuleProjection> findAlerts(
            OperationTargetType targetType,
            OperationAlertStatus status,
            int page,
            int size
    );
}