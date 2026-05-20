package com.wanted.codebombalms.domain.admin.operation.rule.application.usecase;

import com.wanted.codebombalms.domain.admin.operation.rule.application.command.CreateAutomationRuleCommand;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.model.AutomationRule;

public interface CreateAutomationRuleUseCase {
    AutomationRule create(CreateAutomationRuleCommand command);
}