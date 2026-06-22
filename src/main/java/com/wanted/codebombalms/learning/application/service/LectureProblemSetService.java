package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.learning.application.command.RecordLectureProblemProgressCommand;
import com.wanted.codebombalms.learning.application.policy.LearningAccessPolicy;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSet;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSetPort;
import com.wanted.codebombalms.learning.application.port.LearningProblemGradingPort;
import com.wanted.codebombalms.learning.application.port.LearningProblemPort;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemProgressCommandUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemSetQueryUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemSubmissionUseCase;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemSubmissionRepository;
import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase.SubmissionView;
import com.wanted.codebombalms.submission.exception.SubmissionErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureProblemSetService implements LectureProblemSetQueryUseCase, LectureProblemSubmissionUseCase {

    private final LearningLectureProblemSetPort learningLectureProblemSetPort;
    private final LearningProblemPort learningProblemPort;
    private final LearningProblemGradingPort learningProblemGradingPort;
    private final LectureProblemProgressCommandUseCase lectureProblemProgressCommandUseCase;
    private final LectureProblemProgressRepository lectureProblemProgressRepository;
    private final LectureProblemSubmissionRepository lectureProblemSubmissionRepository;
    private final LearningAccessPolicy learningAccessPolicy;

    @Override
    @Transactional
    public LectureProblemSetEntryView enterLectureProblemSet(Long userId, Long lectureProblemSetId) {
        LearningLectureProblemSet lectureProblemSet =
                learningLectureProblemSetPort.findLectureProblemSet(lectureProblemSetId);
        learningAccessPolicy.validateLectureProblemSetAccess(userId, lectureProblemSet);
        var problemSet = learningProblemPort.loadProblemSet(lectureProblemSet.problemSetId());
        LectureProblemProgress progress = findOrCreateProgress(userId, lectureProblemSetId);
        Map<Long, LectureProblemSubmission> latestSubmissions =
                findLatestSubmissions(userId, lectureProblemSetId);
        List<ProblemDetailView> problems = problemSet.problems()
                .stream()
                .map(problem -> new ProblemDetailView(
                        problem.problemId(),
                        problem.problemNumber(),
                        problem.title(),
                        problem.content(),
                        problem.problemType(),
                        problem.point(),
                        problem.startCode(),
                        statusOf(problem.problemNumber(), progress, latestSubmissions.get(problem.problemId())),
                        latestSubmissionId(latestSubmissions.get(problem.problemId()))
                ))
                .toList();

        return new LectureProblemSetEntryView(
                lectureProblemSet.lectureProblemSetId(),
                lectureProblemSet.problemSetId(),
                problemSet.title(),
                problemSet.description(),
                progress.getCurrentProblemNumber(),
                currentProblemId(problemSet.problems(), progress),
                problemSet.problems().size(),
                solvedProblemCount(latestSubmissions),
                progress.isCompleted(),
                problems
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LectureProblemSetProgressView findLectureProblemSetProgress(Long userId, Long lectureProblemSetId) {
        LearningLectureProblemSet lectureProblemSet =
                learningLectureProblemSetPort.findLectureProblemSet(lectureProblemSetId);
        learningAccessPolicy.validateLectureProblemSetAccess(userId, lectureProblemSet);
        var problemSet = learningProblemPort.loadProblemSet(lectureProblemSet.problemSetId());
        LectureProblemProgress progress = lectureProblemProgressRepository
                .findByUserIdAndLectureProblemSetId(userId, lectureProblemSetId)
                .orElseGet(() -> LectureProblemProgress.create(userId, lectureProblemSetId));
        Map<Long, LectureProblemSubmission> latestSubmissions =
                findLatestSubmissions(userId, lectureProblemSetId);

        return new LectureProblemSetProgressView(
                lectureProblemSet.lectureProblemSetId(),
                lectureProblemSet.problemSetId(),
                problemSet.problems().size(),
                progress.getCurrentProblemNumber(),
                currentProblemId(problemSet.problems(), progress),
                solvedProblemCount(latestSubmissions),
                progress.isCompleted(),
                problemSet.problems()
                        .stream()
                        .map(problem -> new ProblemProgressItemView(
                                problem.problemId(),
                                problem.problemNumber(),
                                statusOf(
                                        problem.problemNumber(),
                                        progress,
                                        latestSubmissions.get(problem.problemId())
                                )
                        ))
                        .toList()
        );
    }

    @Override
    @Transactional
    public SubmissionView submit(Long lectureProblemSetId, Long problemId, SubmitCodeCommand command) {
        LearningLectureProblemSet lectureProblemSet =
                learningLectureProblemSetPort.findLectureProblemSet(lectureProblemSetId);
        learningAccessPolicy.validateLectureProblemSetAccess(command.userId(), lectureProblemSet);

        if (!learningProblemPort.existsProblem(problemId)) {
            throw new NotFoundException(LearningErrorCode.PROBLEM_NOT_FOUND);
        }

        if (!learningProblemPort.existsProblemInSet(lectureProblemSet.problemSetId(), problemId)) {
            throw new NotFoundException(LearningErrorCode.PROBLEM_NOT_IN_LECTURE_PROBLEM_SET);
        }

        var problem = learningProblemPort.loadProblem(problemId);
        LectureProblemProgress progress = lockProgress(command.userId(), lectureProblemSetId);
        validateSubmissionProgress(progress, problem.problemNumber());

        int previousAttemptCount = lectureProblemSubmissionRepository.countAttempts(
                command.userId(),
                lectureProblemSetId,
                problemId
        );
        validateAttempt(problem.attemptLimit(), problem.retriable(), previousAttemptCount);

        var gradingResult = learningProblemGradingPort.grade(
                lectureProblemSet.problemSetId(),
                problemId,
                command.code()
        );
        int attemptNo = previousAttemptCount + 1;
        LectureProblemSubmission savedSubmission = lectureProblemSubmissionRepository.save(
                LectureProblemSubmission.create(
                        command.userId(),
                        lectureProblemSetId,
                        problemId,
                        command.code(),
                        gradingResult.correct(),
                        attemptNo,
                        gradingResult.passedTestCount(),
                        gradingResult.totalTestCount(),
                        gradingResult.executionStatus(),
                        gradingResult.errorMessage()
                )
        );

        Long nextProblemId = null;
        boolean completed = false;
        if (gradingResult.correct()) {
            var problemSet = learningProblemPort.loadProblemSet(lectureProblemSet.problemSetId());
            nextProblemId = findNextProblemId(problemSet.problems(), problem.problemNumber());
            completed = nextProblemId == null;
            int nextProblemNumber = completed
                    ? problem.problemNumber()
                    : problem.problemNumber() + 1;
            recordLectureProblemProgress(
                    command.userId(),
                    lectureProblemSet,
                    nextProblemNumber,
                    completed
            );
        }

        Integer remainingAttemptCount = calculateRemainingAttempts(problem.attemptLimit(), attemptNo);
        boolean canRetry = !gradingResult.correct()
                && Boolean.TRUE.equals(problem.retriable())
                && (remainingAttemptCount == null || remainingAttemptCount > 0);

        return new SubmissionView(
                savedSubmission.lectureProblemSubmissionId(),
                problemId,
                gradingResult.correct(),
                gradingResult.passedTestCount(),
                gradingResult.totalTestCount(),
                gradingResult.executionStatus(),
                gradingResult.errorMessage(),
                attemptNo,
                remainingAttemptCount,
                canRetry,
                nextProblemId,
                completed,
                0,
                false,
                gradingResult.correct() ? problem.explanation() : null
        );
    }

    private LectureProblemProgress findOrCreateProgress(Long userId, Long lectureProblemSetId) {
        return lectureProblemProgressRepository
                .findByUserIdAndLectureProblemSetId(userId, lectureProblemSetId)
                .orElseGet(() -> lectureProblemProgressCommandUseCase.recordProgress(
                        new RecordLectureProblemProgressCommand(userId, lectureProblemSetId, 1, false)
                ));
    }

    private LectureProblemProgress lockProgress(Long userId, Long lectureProblemSetId) {
        LectureProblemProgress progress = findOrCreateProgress(userId, lectureProblemSetId);
        return lectureProblemProgressRepository
                .findByUserIdAndLectureProblemSetIdForUpdate(userId, lectureProblemSetId)
                .orElse(progress);
    }

    private Map<Long, LectureProblemSubmission> findLatestSubmissions(
            Long userId,
            Long lectureProblemSetId
    ) {
        Map<Long, LectureProblemSubmission> latestSubmissions = new LinkedHashMap<>();
        lectureProblemSubmissionRepository.findByUserIdAndLectureProblemSetId(userId, lectureProblemSetId)
                .forEach(submission -> latestSubmissions.putIfAbsent(submission.problemId(), submission));
        return latestSubmissions;
    }

    private String statusOf(
            Integer problemNumber,
            LectureProblemProgress progress,
            LectureProblemSubmission submission
    ) {
        if (!progress.isCompleted() && problemNumber > progress.getCurrentProblemNumber()) {
            return "LOCKED";
        }
        if (submission == null) {
            return "UNSOLVED";
        }
        return submission.correct() ? "CORRECT" : "WRONG";
    }

    private Long latestSubmissionId(LectureProblemSubmission submission) {
        return submission == null ? null : submission.lectureProblemSubmissionId();
    }

    private int solvedProblemCount(Map<Long, LectureProblemSubmission> latestSubmissions) {
        return (int) latestSubmissions.values()
                .stream()
                .filter(LectureProblemSubmission::correct)
                .count();
    }

    private Long currentProblemId(
            List<LearningProblemPort.ProblemDetailForLearning> problems,
            LectureProblemProgress progress
    ) {
        return problems.stream()
                .filter(problem -> problem.problemNumber().equals(progress.getCurrentProblemNumber()))
                .map(LearningProblemPort.ProblemDetailForLearning::problemId)
                .findFirst()
                .orElse(null);
    }

    private void validateSubmissionProgress(LectureProblemProgress progress, Integer problemNumber) {
        if (progress.isCompleted()) {
            throw new ConflictException(LearningErrorCode.LECTURE_PROBLEM_SET_ALREADY_COMPLETED);
        }
        if (!progress.getCurrentProblemNumber().equals(problemNumber)) {
            throw new ValidationException(LearningErrorCode.LECTURE_PROBLEM_NOT_UNLOCKED);
        }
    }

    private void validateAttempt(Integer attemptLimit, Boolean retriable, int previousAttemptCount) {
        if (!Boolean.TRUE.equals(retriable) && previousAttemptCount > 0) {
            throw new ValidationException(SubmissionErrorCode.PROBLEM_NOT_RETRIABLE);
        }
        if (attemptLimit != null && previousAttemptCount >= attemptLimit) {
            throw new ValidationException(SubmissionErrorCode.ATTEMPT_LIMIT_EXCEEDED);
        }
    }

    private Integer calculateRemainingAttempts(Integer attemptLimit, int attemptNo) {
        return attemptLimit == null ? null : Math.max(attemptLimit - attemptNo, 0);
    }

    private Long findNextProblemId(
            List<LearningProblemPort.ProblemDetailForLearning> problems,
            Integer currentProblemNumber
    ) {
        return problems.stream()
                .filter(problem -> problem.problemNumber().equals(currentProblemNumber + 1))
                .map(LearningProblemPort.ProblemDetailForLearning::problemId)
                .findFirst()
                .orElse(null);
    }

    private void recordLectureProblemProgress(
            Long userId,
            LearningLectureProblemSet lectureProblemSet,
            Integer currentProblemNumber,
            boolean completed
    ) {
        lectureProblemProgressCommandUseCase.recordProgress(new RecordLectureProblemProgressCommand(
                userId,
                lectureProblemSet.lectureProblemSetId(),
                currentProblemNumber,
                completed
        ));
    }
}
