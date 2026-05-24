package com.wanted.codebombalms.admin.operation.automation.application.handler;

import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.automation.application.port.CourseOperationMetricPort;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
// 수강생 수가 기준 이하인 강좌를 탐지하는 규칙 핸들러다.
public class CourseLowEnrollmentRuleHandler implements OperationRuleHandler {

    private final CourseOperationMetricPort courseOperationMetricPort;

    @Override
    public OperationRuleCode supports() {
        return OperationRuleCode.COURSE_LOW_ENROLLMENT;
    }

    @Override
    public List<OperationRuleDetectionResult> detect(AutomationRule rule) {
        return courseOperationMetricPort.findLowEnrollmentCourses(rule.getThresholdValue());
    }
}
