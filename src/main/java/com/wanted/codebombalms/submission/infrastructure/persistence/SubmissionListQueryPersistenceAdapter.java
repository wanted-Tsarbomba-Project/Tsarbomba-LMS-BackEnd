package com.wanted.codebombalms.submission.infrastructure.persistence;

import com.wanted.codebombalms.submission.application.port.SubmissionListQueryPort;
import com.wanted.codebombalms.submission.domain.model.CodeSubmissionListItem;
import com.wanted.codebombalms.submission.domain.model.CodeSubmissionPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class SubmissionListQueryPersistenceAdapter implements SubmissionListQueryPort {

    private final SpringDataSubmissionRepository submissionRepository;

    public SubmissionListQueryPersistenceAdapter(SpringDataSubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    public CodeSubmissionPage findCodeSubmissions(Long problemId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<SubmissionJpaEntity> result = submissionRepository
                .findByProblem_ProblemIdAndSubmittedCodeIsNotNullOrderBySubmittedAtDesc(problemId, pageRequest);

        return new CodeSubmissionPage(
                result.getContent()
                        .stream()
                        .map(this::toListItem)
                        .toList(),
                result.getNumber() + 1,
                result.getTotalPages(),
                result.getTotalElements()
        );
    }

    private CodeSubmissionListItem toListItem(SubmissionJpaEntity submission) {
        return new CodeSubmissionListItem(
                submission.getSubmissionId(),
                submission.getProblem().getProblemId(),
                submission.getCorrect(),
                submission.getPassedTestCount(),
                submission.getTotalTestCount(),
                submission.getExecutionStatus(),
                submission.getSubmittedAt()
        );
    }
}