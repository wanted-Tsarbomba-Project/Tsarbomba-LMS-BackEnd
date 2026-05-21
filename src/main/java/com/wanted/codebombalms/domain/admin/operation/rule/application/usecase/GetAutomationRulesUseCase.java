package com.wanted.codebombalms.domain.admin.operation.rule.application.usecase;

import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.model.AutomationRule;

import java.util.List;

public interface GetAutomationRulesUseCase {
    List<AutomationRule> getRules(OperationTargetType targetType);
}