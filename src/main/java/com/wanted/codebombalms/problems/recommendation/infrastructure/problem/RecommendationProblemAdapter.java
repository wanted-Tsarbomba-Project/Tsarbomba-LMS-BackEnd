package com.wanted.codebombalms.problems.recommendation.infrastructure.problem;

import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.problems.recommendation.application.port.LoadRecommendationProblemPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationProblemAdapter implements LoadRecommendationProblemPort {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final SpringDataProblemRepository problemRepository;

    @Override
    public boolean existsActiveProblem(Long problemId) {
        return problemRepository.findByProblemIdAndStatus(problemId, ACTIVE_STATUS)
                .isPresent();
    }
}
