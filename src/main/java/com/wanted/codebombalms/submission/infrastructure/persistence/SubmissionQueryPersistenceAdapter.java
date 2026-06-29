package com.wanted.codebombalms.submission.infrastructure.persistence;

import com.wanted.codebombalms.submission.domain.model.LatestSubmission;
import com.wanted.codebombalms.submission.application.port.SubmissionQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SubmissionQueryPersistenceAdapter implements SubmissionQueryPort {

    private final SpringDataSubmissionRepository submissionRepository;

    public SubmissionQueryPersistenceAdapter(SpringDataSubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    public Optional<LatestSubmission> findLatestResult(Long userId, Long problemId) {
        return submissionRepository
                .findTopByUserIdAndProblem_ProblemIdOrderBySubmittedAtDesc(userId, problemId)
                .map(this::toResult);
    }

    private LatestSubmission toResult(SubmissionJpaEntity submission) {
        return new LatestSubmission(
                submission.getSubmissionId(),
                submission.getProblem().getProblemId(),
                submission.getProblem().getProblemOrder(),
                submission.getSubmittedCode(),
                submission.getCorrect(),
                submission.getSubmittedAt()
        );
    }

    @Override
    public List<LatestSubmission> findLatestResults(Long userId, List<Long> problemIds) {
        if (problemIds.isEmpty()) {
            return List.of();
        }

        return submissionRepository.findLatestByUserIdAndProblemIds(userId, problemIds)
                .stream()
                .map(this::toResult)
                .toList();
    }
}
