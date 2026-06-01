package com.wanted.codebombalms.submission.infrastructure.persistence;

import com.wanted.codebombalms.submission.application.port.ProblemSubmissionMetricPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProblemSubmissionMetricPersistenceAdapter implements ProblemSubmissionMetricPort {

    private final SpringDataSubmissionRepository submissionRepository;

    @Override
    public List<ProblemWrongRateMetric> findProblemWrongRateMetrics(Integer minSampleCount) {
        return submissionRepository.findProblemWrongRateMetrics(minSampleCount.longValue());
    }
}
