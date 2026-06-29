package com.wanted.codebombalms.admin.operation.automation.application.handler;

import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;

import java.util.List;
import java.util.function.Consumer;

// 규칙 코드별 탐지 로직을 실행하는 전략 인터페이스다.
public interface OperationRuleHandler {

    OperationRuleCode supports();

    List<OperationRuleDetectionResult> detect(AutomationRule rule);

    default int detect(AutomationRule rule, Consumer<OperationRuleDetectionResult> resultConsumer) {
        List<OperationRuleDetectionResult> results = detect(rule);
        results.forEach(resultConsumer);
        return results.size();
    }
}
