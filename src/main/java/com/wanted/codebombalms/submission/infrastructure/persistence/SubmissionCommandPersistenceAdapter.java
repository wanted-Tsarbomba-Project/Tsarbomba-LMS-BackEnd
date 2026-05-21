package com.wanted.codebombalms.submission.infrastructure.persistence;

import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.submission.domain.model.TextSubmission;
import com.wanted.codebombalms.submission.application.port.SubmissionCommandPort;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class SubmissionCommandPersistenceAdapter implements SubmissionCommandPort {

    private final SpringDataSubmissionRepository submissionRepository;
    private final SpringDataProblemRepository problemRepository;

    public SubmissionCommandPersistenceAdapter(
            SpringDataSubmissionRepository submissionRepository,
            SpringDataProblemRepository problemRepository
    ) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
    }

    @Override
    public int countAttempts(Long userId, Long problemId) {
        return submissionRepository.countByUserIdAndProblem_ProblemId(userId, problemId);
    }

    @Override
    public void saveTextSubmission(TextSubmission submission) {
        ProblemJpaEntity problem = problemRepository.findById(submission.problemId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        SubmissionJpaEntity submissionEntity = new SubmissionJpaEntity(
                submission.userId(),
                problem,
                submission.submittedAnswer(),
                submission.correct(),
                submission.earnedScore(),
                submission.attemptNo()
        );

        submissionRepository.save(submissionEntity);
    }
}
