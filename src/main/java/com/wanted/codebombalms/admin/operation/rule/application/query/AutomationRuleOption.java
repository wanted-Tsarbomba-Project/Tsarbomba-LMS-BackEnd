package com.wanted.codebombalms.admin.operation.rule.application.query;

import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;

public record AutomationRuleOption(
        OperationRuleCode ruleCode,
        boolean creatable
) {
}
