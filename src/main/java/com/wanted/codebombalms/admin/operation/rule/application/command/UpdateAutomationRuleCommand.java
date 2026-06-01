package com.wanted.codebombalms.admin.operation.rule.application.command;

import java.math.BigDecimal;
import java.util.List;

// presentation 계층의 request를 application 계층으로 전달
public record UpdateAutomationRuleCommand(
        List<Item> rules
) {
    public record Item(
            Long operationRuleId,
            BigDecimal thresholdValue,
            Integer minSampleCount
    ) {
    }
}
