package com.wanted.codebombalms.admin.operation.rule.application.usecase;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;

import java.util.List;

public interface GetAutomationRulesUseCase {
    List<AutomationRule> getRules(OperationTargetType targetType);
}