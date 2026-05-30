package com.wanted.codebombalms.submission.application.usecase;

import java.math.BigDecimal;
import java.util.List;

public interface ProblemSubmissionMetricQueryUseCase {

    List<ProblemWrongRateView> findHighWrongRateProblems(
            BigDecimal wrongRateThreshold,
            Integer minSampleCount
    );

    record ProblemWrongRateView(
            Long problemId,
            Long submissionCount,
            Long wrongCount,
            BigDecimal wrongRate
    ) {
    }
}
