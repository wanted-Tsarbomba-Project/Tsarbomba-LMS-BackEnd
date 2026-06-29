package com.wanted.codebombalms.admin.operation.automation.application.handler;

import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.automation.application.port.UserOperationMetricPort;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
// 마지막 활동일이 기준보다 오래된 학생을 탐지하는 규칙 핸들러다.
public class UserInactiveNoCourseRuleHandler implements OperationRuleHandler {

    private final UserOperationMetricPort userOperationMetricPort;

    @Override
    public OperationRuleCode supports() {
        return OperationRuleCode.USER_INACTIVE_NO_COURSE;
    }

    @Override
    public List<OperationRuleDetectionResult> detect(AutomationRule rule) {
        return userOperationMetricPort.findInactiveUsers(rule.getThresholdValue());
    }

    @Override
    public int detect(AutomationRule rule, Consumer<OperationRuleDetectionResult> resultConsumer) {
        return userOperationMetricPort.findInactiveUsers(
                rule.getThresholdValue(),
                resultConsumer
        );
    }
}
