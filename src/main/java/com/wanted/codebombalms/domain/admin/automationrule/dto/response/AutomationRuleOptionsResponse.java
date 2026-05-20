package com.wanted.codebombalms.domain.admin.automationrule.dto.response;

import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationRuleCode;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationSeverity;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationTargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class AutomationRuleOptionsResponse {

    private List<RuleOption> ruleOptions;
    private List<OperationSeverity> severities;

    public static AutomationRuleOptionsResponse from(
            List<OperationRuleCode> ruleCodes,
            List<OperationSeverity> severities
    ) {
        return new AutomationRuleOptionsResponse(
                ruleCodes.stream()
                        .map(RuleOption::from)
                        .toList(),
                severities
        );
    }

    @Getter
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