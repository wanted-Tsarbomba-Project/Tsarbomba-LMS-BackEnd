package com.wanted.codebombalms.domain.problems.set.service;

import com.wanted.codebombalms.domain.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
import com.wanted.codebombalms.domain.problems.problem.repository.ProblemRepository;
import com.wanted.codebombalms.domain.problems.set.dto.response.ProblemSetDeleteResponse;
import com.wanted.codebombalms.domain.problems.set.entity.ProblemSet;
import com.wanted.codebombalms.domain.problems.set.repository.ProblemSetRepository;
import com.wanted.codebombalms.domain.submission.repository.SubmissionRepository;
import com.wanted.codebombalms.global.error.exception.ConflictException;
import com.wanted.codebombalms.global.error.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProblemSetDeleteService {

    private final ProblemSetRepository problemSetRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

    public ProblemSetDeleteService(
            ProblemSetRepository problemSetRepository,
            ProblemRepository problemRepository,
            SubmissionRepository submissionRepository
    ) {
        this.problemSetRepository = problemSetRepository;
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional
    public ProblemSetDeleteResponse deactivateProblemSet(Long problemSetId, boolean force) {
        ProblemSet problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        boolean hasSubmission = submissionRepository.existsByProblem_ProblemSet_ProblemSetId(problemSetId);

        if (hasSubmission && !force) {
            throw new ConflictException(ProblemErrorCode.PROBLEM_HAS_SUBMISSION);
        }

        List<Problem> problems = problemRepository.findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(
                problemSetId,
                "ACTIVE"
        );

        problemSet.deactivate();
        problems.forEach(Problem::deactivate);

        return new ProblemSetDeleteResponse(
                problemSet.getProblemSetId(),
                "INACTIVE",
                problems.size()
        );
    }
}
