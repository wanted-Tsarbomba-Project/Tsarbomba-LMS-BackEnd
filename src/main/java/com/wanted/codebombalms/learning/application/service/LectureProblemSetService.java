package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.learning.application.command.RecordLectureProblemProgressCommand;
import com.wanted.codebombalms.learning.application.policy.LearningAccessPolicy;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSet;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSetPort;
import com.wanted.codebombalms.learning.application.port.LearningProblemExplanationPort;
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
import com.wanted.codebombalms.problems.explanation.application.usecase.ViewProblemExplanationUseCase.ExplanationView;
import com.wanted.codebombalms.problems.progress.enums.ProblemProgressStatus;
import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase.SubmissionView;
import com.wanted.codebombalms.submission.exception.SubmissionErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanted.codebombalms.serviceevent.application.port.ServiceEventRecorder;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventType;

@Service
@RequiredArgsConstructor
public class LectureProblemSetService implements LectureProblemSetQueryUseCase, LectureProblemSubmissionUseCase {

    private final LearningLectureProblemSetPort learningLectureProblemSetPort;
    private final LearningProblemPort learningProblemPort;
    private final LearningProblemGradingPort learningProblemGradingPort;
    private final LearningProblemExplanationPort learningProblemExplanationPort;
    private final LectureProblemProgressCommandUseCase lectureProblemProgressCommandUseCase;
    private final LectureProblemProgressRepository lectureProblemProgressRepository;
    private final LectureProblemSubmissionRepository lectureProblemSubmissionRepository;
    private final LearningAccessPolicy learningAccessPolicy;
    private final ServiceEventRecorder serviceEventRecorder;

    @Override
    @Transactional
    public LectureProblemSetEntryView enterLectureProblemSet(Long userId, Long lectureProblemSetId) {
        LearningLectureProblemSet lectureProblemSet =
                learningLectureProblemSetPort.findLectureProblemSet(lectureProblemSetId);
        learningAccessPolicy.validateLectureProblemSetAccess(userId, lectureProblemSet);

        return buildLectureProblemSetEntry(userId, lectureProblemSet, true);
    }

    @Override
    @Transactional(readOnly = true)
    public LectureProblemSetEntryView findStudentLectureProblemSet(
            Long courseId,
            Long userId,
            Long lectureProblemSetId
    ) {
        LearningLectureProblemSet lectureProblemSet =
                learningLectureProblemSetPort.findLectureProblemSet(lectureProblemSetId);
        learningAccessPolicy.validateStudentLectureProblemSetAccess(courseId, userId, lectureProblemSet);

        return buildLectureProblemSetEntry(userId, lectureProblemSet, false);
    }

    private LectureProblemSetEntryView buildLectureProblemSetEntry(
            Long userId,
            LearningLectureProblemSet lectureProblemSet,
            boolean createProgressIfAbsent
    ) {
        var problemSet = learningProblemPort.loadProblemSet(lectureProblemSet.problemSetId());
        LectureProblemProgress progress = findProgress(
                userId,
                lectureProblemSet.lectureProblemSetId(),
                createProgressIfAbsent
        );
        Map<Long, LectureProblemSubmission> latestSubmissions =
                findLatestSubmissions(userId, lectureProblemSet.lectureProblemSetId());
        Set<Long> viewedProblemIds = findViewedProblemIds(userId, problemSet.problems());
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
                        statusOf(
                                problem.problemId(),
                                problem.problemNumber(),
                                progress,
                                latestSubmissions.get(problem.problemId()),
                                viewedProblemIds
                        ),
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
        Set<Long> viewedProblemIds = findViewedProblemIds(userId, problemSet.problems());

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
                                        problem.problemId(),
                                        problem.problemNumber(),
                                        progress,
                                        latestSubmissions.get(problem.problemId()),
                                        viewedProblemIds
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
        if (gradingResult.correct() && isCurrentProblem(progress, problem.problemNumber())) {
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
            if (completed) {
                serviceEventRecorder.record(ServiceEventEnvelope.business(
                        ServiceEventType.PROBLEM_SET_COMPLETED, command.userId(),
                        lectureProblemSet.problemSetId(),
                        "source=lecture lectureProblemSetId=" + lectureProblemSetId));
            }
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

    @Override
    @Transactional
    public ExplanationView viewExplanation(Long userId, Long lectureProblemSetId, Long problemId) {
        LearningLectureProblemSet lectureProblemSet =
                learningLectureProblemSetPort.findLectureProblemSet(lectureProblemSetId);
        learningAccessPolicy.validateLectureProblemSetAccess(userId, lectureProblemSet);

        if (!learningProblemPort.existsProblem(problemId)) {
            throw new NotFoundException(LearningErrorCode.PROBLEM_NOT_FOUND);
        }
        if (!learningProblemPort.existsProblemInSet(lectureProblemSet.problemSetId(), problemId)) {
            throw new NotFoundException(LearningErrorCode.PROBLEM_NOT_IN_LECTURE_PROBLEM_SET);
        }

        var problem = learningProblemPort.loadProblem(problemId);

        LectureProblemSubmission latestSubmission =
                findLatestSubmissions(userId, lectureProblemSetId).get(problemId);
        if (latestSubmission != null && latestSubmission.correct()) {
            return readOnlyExplanation(problem, ProblemProgressStatus.CORRECT);
        }

        if (learningProblemExplanationPort.existsViewed(userId, problemId)) {
            return readOnlyExplanation(problem, ProblemProgressStatus.EXPLANATION_VIEWED);
        }

        LectureProblemProgress progress = lockProgress(userId, lectureProblemSetId);
        validateSubmissionProgress(progress, problem.problemNumber());

        learningProblemExplanationPort.saveViewed(userId, problemId, lectureProblemSet.problemSetId());

        var problemSet = learningProblemPort.loadProblemSet(lectureProblemSet.problemSetId());
        Long nextProblemId = findNextProblemId(problemSet.problems(), problem.problemNumber());
        boolean completed = nextProblemId == null;
        int nextProblemNumber = completed ? problem.problemNumber() : problem.problemNumber() + 1;
        recordLectureProblemProgress(userId, lectureProblemSet, nextProblemNumber, completed);

        if (completed) {
            serviceEventRecorder.record(ServiceEventEnvelope.business(
                    ServiceEventType.PROBLEM_SET_COMPLETED, userId,
                    lectureProblemSet.problemSetId(),
                    "source=lecture lectureProblemSetId=" + lectureProblemSetId));
        }

        return new ExplanationView(
                problemId,
                ProblemProgressStatus.EXPLANATION_VIEWED,
                ProblemProgressStatus.CORRECT,
                problem.explanation(),
                nextProblemId,
                completed,
                0,
                false
        );
    }

    private ExplanationView readOnlyExplanation(
            LearningProblemPort.ProblemForLearning problem,
            ProblemProgressStatus status
    ) {
        return new ExplanationView(
                problem.problemId(),
                status,
                ProblemProgressStatus.CORRECT,
                problem.explanation(),
                null,
                false,
                0,
                false
        );
    }

    private LectureProblemProgress findOrCreateProgress(Long userId, Long lectureProblemSetId) {
        return findProgress(userId, lectureProblemSetId, true);
    }

    private LectureProblemProgress findProgress(
            Long userId,
            Long lectureProblemSetId,
            boolean createProgressIfAbsent
    ) {
        return lectureProblemProgressRepository
                .findByUserIdAndLectureProblemSetId(userId, lectureProblemSetId)
                .orElseGet(() -> {
                    if (createProgressIfAbsent) {
                        return lectureProblemProgressCommandUseCase.recordProgress(
                                new RecordLectureProblemProgressCommand(userId, lectureProblemSetId, 1, false)
                        );
                    }
                    return LectureProblemProgress.create(userId, lectureProblemSetId);
                });
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
            Long problemId,
            Integer problemNumber,
            LectureProblemProgress progress,
            LectureProblemSubmission submission,
            Set<Long> viewedProblemIds
    ) {
        if (!progress.isCompleted() && problemNumber > progress.getCurrentProblemNumber()) {
            return "LOCKED";
        }
        if (submission != null && submission.correct()) {
            return "CORRECT";
        }
        if (viewedProblemIds.contains(problemId)) {
            return "EXPLANATION_VIEWED";
        }
        if (submission == null) {
            return "UNSOLVED";
        }
        return "WRONG";
    }

    private Set<Long> findViewedProblemIds(
            Long userId,
            List<LearningProblemPort.ProblemDetailForLearning> problems
    ) {
        return learningProblemExplanationPort.findViewedProblemIds(
                userId,
                problems.stream().map(LearningProblemPort.ProblemDetailForLearning::problemId).toList()
        );
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
        if (problemNumber > progress.getCurrentProblemNumber()) {
            throw new ValidationException(LearningErrorCode.LECTURE_PROBLEM_NOT_UNLOCKED);
        }
    }

    private boolean isCurrentProblem(LectureProblemProgress progress, Integer problemNumber) {
        return progress.getCurrentProblemNumber().equals(problemNumber);
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
