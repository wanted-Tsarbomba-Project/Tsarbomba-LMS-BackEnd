package com.wanted.codebombalms.admin.operation.alert.domain.repository;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;

import java.util.Optional;

public interface OperationAlertRepository {

    Optional<OperationAlert> findById(Long operationAlertId);

    // 같은 규칙과 대상에 대해 아직 처리되지 않은 알림을 조회한다.
    Optional<OperationAlert> findOpenByRuleIdAndTarget(
            Long operationRuleId,
            OperationTargetType targetType,
            Long targetId
    );

    OperationAlert save(OperationAlert operationAlert);
}
