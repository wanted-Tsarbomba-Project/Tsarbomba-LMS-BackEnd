package com.wanted.codebombalms.submission.application.port;

import java.util.List;

public interface ProblemSubmissionMetricPort {

    List<ProblemWrongRateMetric> findProblemWrongRateMetrics(Integer minSampleCount);

    record ProblemWrongRateMetric(
            Long problemId,
            Long submissionCount,
            Long wrongCount
    ) {
    }
}
