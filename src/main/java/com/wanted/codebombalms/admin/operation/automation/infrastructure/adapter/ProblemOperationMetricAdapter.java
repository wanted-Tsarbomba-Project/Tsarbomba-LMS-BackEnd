package com.wanted.codebombalms.admin.operation.automation.infrastructure.adapter;

import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.automation.application.port.ProblemOperationMetricPort;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.submission.application.usecase.ProblemSubmissionMetricQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 문제와 제출 데이터를 조회해 오답률 높은 문제 지표를 만든다.
public class ProblemOperationMetricAdapter implements ProblemOperationMetricPort {

    private final ProblemSubmissionMetricQueryUseCase problemSubmissionMetricQueryUseCase;

    @Override
    public List<OperationRuleDetectionResult> findHighWrongRateProblems(
            BigDecimal wrongRateThreshold,
            Integer minSampleCount
    ) {
        return problemSubmissionMetricQueryUseCase
                .findHighWrongRateProblems(wrongRateThreshold, minSampleCount)
                .stream()
                .map(metric -> toResult(metric, wrongRateThreshold, minSampleCount))
                .toList();
    }

    private OperationRuleDetectionResult toResult(
            ProblemSubmissionMetricQueryUseCase.ProblemWrongRateView metric,
            BigDecimal wrongRateThreshold,
            Integer minSampleCount
    ) {
        return new OperationRuleDetectionResult(
                OperationTargetType.PROBLEM,
                metric.problemId(),
                metric.wrongRate(),
                "오답률이 기준 이상입니다. 현재 오답률: " + metric.wrongRate() + "%, 제출 수: " + metric.submissionCount() + "회",
                "문제 설명, 정답, 테스트 케이스를 점검하세요. 기준값: " + wrongRateThreshold + "%, 최소 제출 수: " + minSampleCount + "회"
        );
    }
}
