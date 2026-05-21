package com.wanted.codebombalms.admin.operation.rule.domain.repository;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;

import java.util.List;
import java.util.Optional;

public interface AutomationRuleRepository {

    AutomationRule save(AutomationRule automationRule);

    Optional<AutomationRule> findById(Long operationRuleId);

    List<AutomationRule> findAllActive(OperationTargetType targetType);

    boolean existsActiveByRuleCode(OperationRuleCode ruleCode);

    List<OperationRuleCode> findActiveRuleCodes();
}