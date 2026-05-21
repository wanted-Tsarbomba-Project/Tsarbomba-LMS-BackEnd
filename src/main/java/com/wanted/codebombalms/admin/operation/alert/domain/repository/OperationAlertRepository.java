package com.wanted.codebombalms.admin.operation.alert.domain.repository;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;

import java.util.Optional;

public interface OperationAlertRepository {

    Optional<OperationAlert> findById(Long operationAlertId);

    OperationAlert save(OperationAlert operationAlert);
}