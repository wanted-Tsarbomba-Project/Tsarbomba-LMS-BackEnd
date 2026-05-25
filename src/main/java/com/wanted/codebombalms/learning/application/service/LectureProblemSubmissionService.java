package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.learning.application.command.SubmitLectureProblemCommand;
import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import com.wanted.codebombalms.learning.application.port.LearningProblemPort;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemSubmissionUseCase;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemSubmissionRepository;
import com.wanted.codebombalms.submission.application.policy.SubmissionAnswerPolicy;
import com.wanted.codebombalms.submission.application.policy.SubmissionAttemptPolicy;
import com.wanted.codebombalms.submission.application.service.AnswerGradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureProblemSubmissionService implements LectureProblemSubmissionUseCase {

    private final LearningCourseProblemPort learningCourseProblemPort;
    private final LearningProblemPort learningProblemPort;
    private final LectureProblemSubmissionRepository lectureProblemSubmissionRepository;
    private final LectureProblemProgressRepository lectureProblemProgressRepository;
    private final SubmissionAnswerPolicy submissionAnswerPolicy;
    private final SubmissionAttemptPolicy submissionAttemptPolicy;
    private final AnswerGradingService answerGradingService;

    @Override
    @Transactional
    public LectureProblemSubmissionResult submit(SubmitLectureProblemCommand command) {
        submissionAnswerPolicy.validate(command.submittedAnswer());

        LearningCourseProblemPort.CourseProblemStepInfo step = learningCourseProblemPort
                .findCourseProblemStep(command.courseProblemStepId())
                .orElseThrow(() -> new NotFoundException(LearningErrorCode.COURSE_PROBLEM_STEP_NOT_FOUND));
        LearningProblemPort.ProblemForLearning problem = learningProblemPort.loadProblem(step.problemId());

        int previousAttemptCount = lectureProblemSubmissionRepository.countAttempts(
                command.userId(),
                command.courseProblemStepId()
        );
        submissionAttemptPolicy.validateAttemptLimit(
                problem.attemptLimit(),
                problem.retriable(),
                previousAttemptCount
        );

        boolean correct = answerGradingService.gradeTextAnswer(problem.answer(), command.submittedAnswer());
        int attemptNo = previousAttemptCount + 1;
        int score = correct ? problem.score() : 0;
        int remainingAttemptCount = submissionAttemptPolicy.calculateRemainingAttemptCount(
                problem.attemptLimit(),
                attemptNo
        );
        boolean canRetry = submissionAttemptPolicy.canRetry(problem.retriable(), remainingAttemptCount, correct);

        LectureProblemSubmission savedSubmission = lectureProblemSubmissionRepository.save(
                LectureProblemSubmission.submit(
                        command.userId(),
                        command.courseProblemStepId(),
                        problem.problemId(),
                        command.submittedAnswer(),
                        correct,
                        score,
                        attemptNo
                )
        );

        LectureProblemProgress progress = lectureProblemProgressRepository
                .findByUserIdAndCourseProblemStepId(command.userId(), command.courseProblemStepId())
                .orElseGet(() -> LectureProblemProgress.create(command.userId(), command.courseProblemStepId()));
        if (correct) {
            progress.complete();
        }
        lectureProblemProgressRepository.save(progress);

        return new LectureProblemSubmissionResult(
                savedSubmission.lectureProblemSubmissionId(),
                command.courseProblemStepId(),
                problem.problemId(),
                correct,
                score,
                attemptNo,
                remainingAttemptCount,
                canRetry,
                progress.isCompleted(),
                correct ? problem.explanation() : null
        );
    }
}
