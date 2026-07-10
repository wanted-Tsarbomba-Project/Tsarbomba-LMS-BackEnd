package com.wanted.codebombalms.submission.infrastructure.persistence;

import com.wanted.codebombalms.problems.explanation.application.port.ProblemExplanationSolvedQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemExplanationSolvedQueryAdapter implements ProblemExplanationSolvedQueryPort {

    private final SpringDataSubmissionRepository submissionRepository;

    @Override
    public boolean existsCorrectSubmission(Long userId, Long problemId) {
        return submissionRepository.existsByUserIdAndProblem_ProblemIdAndIsCorrectTrue(
                userId,
                problemId
        );
    }
}
