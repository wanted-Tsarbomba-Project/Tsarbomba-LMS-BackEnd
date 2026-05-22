package com.wanted.codebombalms.admin.operation.rule.presentation.api.request;

import com.wanted.codebombalms.admin.operation.rule.application.command.UpdateAutomationRuleEnabledCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

//http 수정 요청값
@Getter
@NoArgsConstructor
public class AutomationRuleEnabledUpdateRequest {

    private Boolean enabled;

    public UpdateAutomationRuleEnabledCommand toCommand(Long operationRuleId) {
        return new UpdateAutomationRuleEnabledCommand(
                operationRuleId,
                enabled
        );
    }
}
