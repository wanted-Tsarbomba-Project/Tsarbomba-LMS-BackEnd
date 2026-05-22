package com.wanted.codebombalms.admin.operation.rule.application.command;

import java.math.BigDecimal;

// presentation 계층의 request를 application 계층으로 전달
public record UpdateAutomationRuleCommand(
        Long operationRuleId,
        BigDecimal thresholdValue,
        Integer minSampleCount
) {
}
