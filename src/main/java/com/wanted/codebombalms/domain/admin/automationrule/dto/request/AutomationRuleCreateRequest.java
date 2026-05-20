package com.wanted.codebombalms.domain.admin.automationrule.dto.request;

import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationRuleCode;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationSeverity;
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
}