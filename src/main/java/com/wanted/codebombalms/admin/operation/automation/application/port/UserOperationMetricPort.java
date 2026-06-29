package com.wanted.codebombalms.admin.operation.automation.application.port;

import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

// 사용자 도메인의 운영 지표를 자동 규칙 실행에 제공한다.
public interface UserOperationMetricPort {

    List<OperationRuleDetectionResult> findInactiveUsers(BigDecimal inactiveDaysThreshold);

    default int findInactiveUsers(
            BigDecimal inactiveDaysThreshold,
            Consumer<OperationRuleDetectionResult> resultConsumer
    ) {
        List<OperationRuleDetectionResult> results = findInactiveUsers(inactiveDaysThreshold);
        results.forEach(resultConsumer);
        return results.size();
    }
}
