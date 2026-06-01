package com.wanted.codebombalms.admin.operation.rule.presentation.api.request;

import com.wanted.codebombalms.admin.operation.rule.application.command.UpdateAutomationRuleCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

//threshold 수정 요청 DTO
@Getter
@NoArgsConstructor
public class AutomationRuleUpdateRequest {

    private List<Rule> rules;

    public UpdateAutomationRuleCommand toCommand() {
        return new UpdateAutomationRuleCommand(
                rules == null ? null : rules.stream()
                        .map(rule -> new UpdateAutomationRuleCommand.Item(
                                rule.operationRuleId,
                                rule.thresholdValue,
                                rule.minSampleCount
                        ))
                        .toList()
        );
    }

    @Getter
    @NoArgsConstructor
    public static class Rule {
        private Long operationRuleId;
        private BigDecimal thresholdValue;
        private Integer minSampleCount;
    }
}
