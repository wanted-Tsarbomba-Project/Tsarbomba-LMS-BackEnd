package com.wanted.codebombalms.domain.admin.operation.rule.application.command;

import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.model.OperationRuleCode;

import java.math.BigDecimal;

public record CreateAutomationRuleCommand(
        Long createdBy,
        OperationRuleCode ruleCode,
        BigDecimal thresholdValue,
        Integer minSampleCount,
        OperationSeverity severity,
        Boolean enabled
) {
}