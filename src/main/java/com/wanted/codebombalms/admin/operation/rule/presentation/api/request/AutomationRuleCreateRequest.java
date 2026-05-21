package com.wanted.codebombalms.admin.operation.rule.presentation.api.request;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.admin.operation.rule.application.command.CreateAutomationRuleCommand;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class AutomationRuleCreateRequest {

    private OperationRuleCode ruleCode;
    private BigDecimal thresholdValue;
    private Integer minSampleCount;
    private OperationSeverity severity;
    private Boolean enabled;

    public CreateAutomationRuleCommand toCommand(Long createdBy) {
        return new CreateAutomationRuleCommand(
                createdBy,
                ruleCode,
                thresholdValue,
                minSampleCount,
                severity,
                enabled
        );
    }
}