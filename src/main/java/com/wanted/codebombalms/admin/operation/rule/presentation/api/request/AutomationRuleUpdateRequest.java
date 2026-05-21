package com.wanted.codebombalms.admin.operation.rule.presentation.api.request;

import com.wanted.codebombalms.admin.operation.rule.application.command.UpdateAutomationRuleCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

//threshold 수정 요청 DTO
@Getter
@NoArgsConstructor
public class AutomationRuleUpdateRequest {

    private BigDecimal thresholdValue;
    private Integer minSampleCount;

    public UpdateAutomationRuleCommand toCommand(Long operationRuleId) {
        return new UpdateAutomationRuleCommand(
                operationRuleId,
                thresholdValue,
                minSampleCount
        );
    }
}
