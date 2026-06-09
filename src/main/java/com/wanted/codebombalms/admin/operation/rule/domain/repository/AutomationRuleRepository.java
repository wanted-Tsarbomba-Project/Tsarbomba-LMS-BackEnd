package com.wanted.codebombalms.admin.operation.rule.domain.repository;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;

import java.util.List;
import java.util.Optional;

public interface AutomationRuleRepository {

    AutomationRule save(AutomationRule automationRule);

    Optional<AutomationRule> findById(Long operationRuleId);

    List<AutomationRule> findAllActive(OperationTargetType targetType);

    // 스케줄러가 실행할 활성화된 자동 규칙만 조회한다.
    List<AutomationRule> findEnabled();
}
