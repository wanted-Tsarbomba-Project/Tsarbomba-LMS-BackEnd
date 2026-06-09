package com.wanted.codebombalms.admin.operation.rule.presentation.api.response;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AutomationRuleResponse {

    private Long operationRuleId;
    private OperationRuleCode ruleCode;
    private OperationTargetType targetType;
    private BigDecimal thresholdValue;
    private BigDecimal thresholdMin;
    private BigDecimal thresholdMax;
    private Integer minSampleCount;
    private boolean enabled;

    public static AutomationRuleResponse from(AutomationRule rule) {
        OperationRuleCode ruleCode = rule.getRuleCode();

        return new AutomationRuleResponse(
                rule.getOperationRuleId(),
                rule.getRuleCode(),
                rule.getTargetType(),
                rule.getThresholdValue(),
                ruleCode.getThresholdMin(),
                ruleCode.getThresholdMax(),
                rule.getMinSampleCount(),
                rule.isEnabled()
        );
    }
}
