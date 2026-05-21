package com.wanted.codebombalms.admin.operation.rule.application.command;

// enabled 변경을 application 계층으로 전달하는 command
public record UpdateAutomationRuleEnabledCommand(
        Long operationRuleId,
        Boolean enabled
) {
}
