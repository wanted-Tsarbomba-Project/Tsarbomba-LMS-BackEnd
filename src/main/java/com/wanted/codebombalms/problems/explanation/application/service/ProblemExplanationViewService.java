package com.wanted.codebombalms.problems.explanation.application.service;

import com.wanted.codebombalms.problems.explanation.application.port.ProblemExplanationViewCommandPort;
import com.wanted.codebombalms.problems.explanation.application.usecase.ViewProblemExplanationUseCase;
import com.wanted.codebombalms.problems.progress.enums.ProblemProgressStatus;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort;
import com.wanted.codebombalms.submission.application.port.ProblemProgressPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ProblemExplanationViewService implements ViewProblemExplanationUseCase {

    private final LoadProblemForSubmissionPort loadProblemForSubmissionPort;
    private final ProblemProgressPort problemProgressPort;
    private final ProblemExplanationViewCommandPort commandPort;

    @Override
    @Transactional
    public ViewProblemExplanationUseCase.ExplanationView viewExplanation(Long userId, Long problemId) {
        var problem = loadProblemForSubmissionPort.loadProblemForSubmission(problemId);

        problemProgressPort.validateCurrentProblem(
                userId,
                problem.problemSetId(),
                problem.problemOrder()
        );

        commandPort.saveViewed(
                userId,
                problem.problemId(),
                problem.problemSetId()
        );

        Long nextProblemId = loadProblemForSubmissionPort
                .findNextProblemId(
                        problem.problemSetId(),
                        problem.problemOrder() + 1
                )
                .orElse(null);

        boolean problemSetCompleted = false;

        if (nextProblemId == null) {
            problemSetCompleted = true;
            problemProgressPort.completeProblemSet(
                    userId,
                    problem.problemSetId()
            );
        } else {
            problemProgressPort.openNextProblem(
                    userId,
                    problem.problemSetId()
            );
        }

        return new ViewProblemExplanationUseCase.ExplanationView(
                problem.problemId(),
                ProblemProgressStatus.EXPLANATION_VIEWED,
                ProblemProgressStatus.CORRECT,
                problem.explanation(),
                nextProblemId,
                problemSetCompleted,
                0,
                false
        );
    }
}
