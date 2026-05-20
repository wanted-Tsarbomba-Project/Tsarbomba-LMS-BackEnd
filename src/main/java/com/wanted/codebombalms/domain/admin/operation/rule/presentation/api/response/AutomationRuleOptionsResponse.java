package com.wanted.codebombalms.domain.admin.operation.rule.presentation.api.response;

import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.model.OperationRuleCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AutomationRuleOptionsResponse {

    private List<RuleOption> ruleOptions;
    private List<OperationSeverity> severities;

    public static AutomationRuleOptionsResponse from(List<OperationRuleCode> ruleCodes) {
        return new AutomationRuleOptionsResponse(
                ruleCodes.stream().map(RuleOption::from).toList(),
                Arrays.asList(OperationSeverity.values())
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleOption {

        private OperationRuleCode ruleCode;
        private String label;
        private String description;
        private OperationTargetType targetType;
        private String thresholdLabel;
        private String thresholdUnit;
        private BigDecimal thresholdMin;
        private BigDecimal thresholdMax;
        private boolean requiresMinSampleCount;
        private String minSampleCountLabel;
        private String minSampleCountUnit;

        public static RuleOption from(OperationRuleCode ruleCode) {
            return new RuleOption(
                    ruleCode,
                    ruleCode.getLabel(),
                    ruleCode.getDescription(),
                    ruleCode.getTargetType(),
                    ruleCode.getThresholdLabel(),
                    ruleCode.getThresholdUnit(),
                    ruleCode.getThresholdMin(),
                    ruleCode.getThresholdMax(),
                    ruleCode.isRequiresMinSampleCount(),
                    ruleCode.getMinSampleCountLabel(),
                    ruleCode.getMinSampleCountUnit()
            );
        }
    }
}