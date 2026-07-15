package com.wanted.codebombalms.learning.infrastructure.problem;

import com.wanted.codebombalms.learning.application.port.LearningProblemExplanationPort;
import com.wanted.codebombalms.problems.explanation.application.port.ProblemExplanationViewCommandPort;
import com.wanted.codebombalms.problems.explanation.application.port.ProblemExplanationViewQueryPort;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LearningProblemExplanationAdapter implements LearningProblemExplanationPort {

    private final ProblemExplanationViewQueryPort problemExplanationViewQueryPort;
    private final ProblemExplanationViewCommandPort problemExplanationViewCommandPort;

    @Override
    public boolean existsViewed(Long userId, Long problemId) {
        return problemExplanationViewQueryPort.existsViewed(userId, problemId);
    }

    @Override
    public Set<Long> findViewedProblemIds(Long userId, List<Long> problemIds) {
        return problemExplanationViewQueryPort.findViewedProblemIds(userId, problemIds);
    }

    @Override
    public void saveViewed(Long userId, Long problemId, Long problemSetId) {
        problemExplanationViewCommandPort.saveViewed(userId, problemId, problemSetId);
    }
}
