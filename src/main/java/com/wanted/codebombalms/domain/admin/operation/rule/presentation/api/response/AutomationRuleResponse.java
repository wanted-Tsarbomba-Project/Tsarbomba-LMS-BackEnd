package com.wanted.codebombalms.domain.admin.operation.rule.presentation.api.response;

import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.model.OperationRuleCode;
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
    private String ruleContent;
    private BigDecimal thresholdValue;
    private String thresholdLabel;
    private String thresholdUnit;
    private Integer minSampleCount;
    private OperationSeverity severity;
    private boolean enabled;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AutomationRuleResponse from(AutomationRule rule) {
        OperationRuleCode ruleCode = rule.getRuleCode();

        return new AutomationRuleResponse(
                rule.getOperationRuleId(),
                rule.getRuleCode(),
                rule.getRuleName(),
                rule.getTargetType(),
                rule.buildRuleContent(),
                rule.getThresholdValue(),
                ruleCode.getThresholdLabel(),
                ruleCode.getThresholdUnit(),
                rule.getMinSampleCount(),
                rule.getSeverity(),
                rule.isEnabled(),
                rule.getCreatedBy(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}