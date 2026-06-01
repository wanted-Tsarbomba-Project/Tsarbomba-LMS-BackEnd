package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.submission.application.port.ProblemSubmissionMetricPort;
import com.wanted.codebombalms.submission.application.usecase.ProblemSubmissionMetricQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemSubmissionMetricQueryService implements ProblemSubmissionMetricQueryUseCase {

    private final ProblemSubmissionMetricPort problemSubmissionMetricPort;

    @Override
    public List<ProblemWrongRateView> findHighWrongRateProblems(
            BigDecimal wrongRateThreshold,
            Integer minSampleCount
    ) {
        return problemSubmissionMetricPort.findProblemWrongRateMetrics(minSampleCount).stream()
                .map(this::toView)
                .filter(metric -> metric.wrongRate().compareTo(wrongRateThreshold) >= 0)
                .toList();
    }

    private ProblemWrongRateView toView(ProblemSubmissionMetricPort.ProblemWrongRateMetric metric) {
        BigDecimal wrongRate = BigDecimal.valueOf(metric.wrongCount())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(metric.submissionCount()), 2, RoundingMode.HALF_UP);

        return new ProblemWrongRateView(
                metric.problemId(),
                metric.submissionCount(),
                metric.wrongCount(),
                wrongRate
        );
    }
}
