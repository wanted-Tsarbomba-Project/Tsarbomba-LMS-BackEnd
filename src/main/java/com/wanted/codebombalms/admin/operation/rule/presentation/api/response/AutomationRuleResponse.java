package com.wanted.codebombalms.admin.operation.rule.presentation.api.response;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AutomationRuleResponse {

    private Long operationRuleId;
    private OperationRuleCode ruleCode;
    private String ruleName;
    private OperationTargetType targetType;
    private BigDecimal thresholdValue;
    private String thresholdLabel;
    private String thresholdUnit;
    private BigDecimal thresholdMin;
    private BigDecimal thresholdMax;
    private Integer minSampleCount;
    private boolean requiresMinSampleCount;
    private String minSampleCountLabel;
    private String minSampleCountUnit;
    private OperationSeverity severity;
    private boolean enabled;
    private LocalDateTime updatedAt;

    public static AutomationRuleResponse from(AutomationRule rule) {
        OperationRuleCode ruleCode = rule.getRuleCode();

        return new AutomationRuleResponse(
                rule.getOperationRuleId(),
                rule.getRuleCode(),
                rule.getRuleName(),
                rule.getTargetType(),
                rule.getThresholdValue(),
                ruleCode.getThresholdLabel(),
                ruleCode.getThresholdUnit(),
                ruleCode.getThresholdMin(),
                ruleCode.getThresholdMax(),
                rule.getMinSampleCount(),
                ruleCode.isRequiresMinSampleCount(),
                ruleCode.getMinSampleCountLabel(),
                ruleCode.getMinSampleCountUnit(),
                rule.getSeverity(),
                rule.isEnabled(),
                rule.getUpdatedAt()
        );
    }
}
