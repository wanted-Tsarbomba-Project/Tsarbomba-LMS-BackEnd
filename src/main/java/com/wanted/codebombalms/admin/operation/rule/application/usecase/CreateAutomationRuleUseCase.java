package com.wanted.codebombalms.admin.operation.rule.application.usecase;

import com.wanted.codebombalms.admin.operation.rule.application.command.CreateAutomationRuleCommand;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;

public interface CreateAutomationRuleUseCase {
    AutomationRule create(CreateAutomationRuleCommand command);
}