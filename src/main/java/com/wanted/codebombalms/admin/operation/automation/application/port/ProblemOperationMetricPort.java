package com.wanted.codebombalms.admin.operation.automation.application.port;

import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;

import java.math.BigDecimal;
import java.util.List;

// 문제 도메인의 운영 지표를 자동 규칙 실행에 제공한다.
public interface ProblemOperationMetricPort {

    List<OperationRuleDetectionResult> findHighWrongRateProblems(
            BigDecimal wrongRateThreshold,
            Integer minSampleCount
    );
}
