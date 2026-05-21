package com.wanted.codebombalms.submission.infrastructure.problemset;

import com.wanted.codebombalms.problems.set.application.port.CheckProblemSetSubmissionPort;
import com.wanted.codebombalms.submission.infrastructure.persistence.SpringDataSubmissionRepository;
import org.springframework.stereotype.Component;

@Component
public class ProblemSetSubmissionAdapter implements CheckProblemSetSubmissionPort {

    private final SpringDataSubmissionRepository submissionRepository;

    public ProblemSetSubmissionAdapter(SpringDataSubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    public boolean existsSubmission(Long problemSetId) {
        return submissionRepository.existsByProblem_ProblemSet_ProblemSetId(problemSetId);
    }
}
