package com.wanted.codebombalms.admin.operation.rule.application.usecase;

import com.wanted.codebombalms.admin.operation.rule.application.query.AutomationRuleOption;

import java.util.List;

public interface GetAutomationRuleOptionsUseCase {
    List<AutomationRuleOption> getOptions();
}
