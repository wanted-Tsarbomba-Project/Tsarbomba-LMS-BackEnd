package com.wanted.codebombalms.admin.operation.automation.application.handler;

import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.automation.application.port.ProblemOperationMetricPort;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
// 제출 수와 오답률 기준을 만족하는 문제를 탐지하는 규칙 핸들러다.
public class ProblemHighWrongRateRuleHandler implements OperationRuleHandler {

    private final ProblemOperationMetricPort problemOperationMetricPort;

    @Override
    public OperationRuleCode supports() {
        return OperationRuleCode.PROBLEM_HIGH_WRONG_RATE;
    }

    @Override
    public List<OperationRuleDetectionResult> detect(AutomationRule rule) {
        return problemOperationMetricPort.findHighWrongRateProblems(
                rule.getThresholdValue(),
                rule.getMinSampleCount()
        );
    }
}
