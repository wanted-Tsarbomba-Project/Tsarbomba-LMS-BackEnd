package com.wanted.codebombalms.admin.operation.rule.presentation.api.response;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.application.query.AutomationRuleOption;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
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

    public static AutomationRuleOptionsResponse from(List<AutomationRuleOption> options) {
        return new AutomationRuleOptionsResponse(
                options.stream().map(RuleOption::from).toList(),
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
        private boolean creatable;

        public static RuleOption from(AutomationRuleOption option) {
            OperationRuleCode ruleCode = option.ruleCode();

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
                    ruleCode.getMinSampleCountUnit(),
                    option.creatable()
            );
        }
    }
}
