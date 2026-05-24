package com.wanted.codebombalms.admin.operation.automation.infrastructure.adapter;

import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.automation.application.port.ProblemOperationMetricPort;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 문제와 제출 데이터를 조회해 오답률 높은 문제 지표를 만든다.
public class ProblemOperationMetricAdapter implements ProblemOperationMetricPort {

    private final EntityManager entityManager;

    @Override
    public List<OperationRuleDetectionResult> findHighWrongRateProblems(
            BigDecimal wrongRateThreshold,
            Integer minSampleCount
    ) {
        return entityManager.createQuery("""
                        select p.problemId,
                               count(s.submissionId),
                               sum(case when s.isCorrect = false then 1 else 0 end)
                        from ProblemJpaEntity p
                        join SubmissionJpaEntity s on s.problem = p
                        where p.status = 'ACTIVE'
                        group by p.problemId
                        having count(s.submissionId) >= :minSampleCount
                        """, Object[].class)
                .setParameter("minSampleCount", minSampleCount.longValue())
                .getResultList()
                .stream()
                .map(this::toMetric)
                .filter(metric -> metric.wrongRate().compareTo(wrongRateThreshold) >= 0)
                .map(metric -> toResult(metric, wrongRateThreshold, minSampleCount))
                .toList();
    }

    private ProblemWrongRateMetric toMetric(Object[] row) {
        Long problemId = (Long) row[0];
        Long submissionCount = (Long) row[1];
        Long wrongCount = (Long) row[2];
        BigDecimal wrongRate = BigDecimal.valueOf(wrongCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(submissionCount), 2, RoundingMode.HALF_UP);

        return new ProblemWrongRateMetric(problemId, submissionCount, wrongCount, wrongRate);
    }

    private OperationRuleDetectionResult toResult(
            ProblemWrongRateMetric metric,
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

    private record ProblemWrongRateMetric(
            Long problemId,
            Long submissionCount,
            Long wrongCount,
            BigDecimal wrongRate
    ) {
    }
}
