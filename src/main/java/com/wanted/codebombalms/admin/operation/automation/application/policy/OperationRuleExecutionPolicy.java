package com.wanted.codebombalms.admin.operation.automation.application.policy;

import com.wanted.codebombalms.admin.operation.automation.application.handler.OperationRuleHandler;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import org.springframework.stereotype.Component;

@Component
// 자동 규칙이 현재 실행 가능한 상태인지 판단한다.
public class OperationRuleExecutionPolicy {

    public boolean canExecute(AutomationRule rule, OperationRuleHandler handler) {
        return rule != null
                && rule.isEnabled()
                && handler != null;
    }
}
