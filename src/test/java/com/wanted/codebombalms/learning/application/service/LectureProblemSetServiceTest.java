package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSet;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSetPort;
import com.wanted.codebombalms.learning.application.port.LearningProblemGradingPort;
import com.wanted.codebombalms.learning.application.port.LearningProblemPort;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemProgressCommandUseCase;
import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemSubmissionRepository;
import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LectureProblemSetServiceTest {

    @Mock
    private LearningLectureProblemSetPort learningLectureProblemSetPort;

    @Mock
    private LearningProblemPort learningProblemPort;

    @Mock
    private LearningProblemGradingPort learningProblemGradingPort;

    @Mock
    private LectureProblemProgressCommandUseCase lectureProblemProgressCommandUseCase;

    @Mock
    private LectureProblemProgressRepository lectureProblemProgressRepository;

    @Mock
    private LectureProblemSubmissionRepository lectureProblemSubmissionRepository;

    @InjectMocks
    private LectureProblemSetService lectureProblemSetService;

    @Test
    void enterLectureProblemSet_usesOnlyLectureProgressAndSubmissions() {
        Long userId = 10L;
        Long lectureProblemSetId = 6001L;
        Long problemSetId = 2001L;
        var progress = progress(userId, lectureProblemSetId, 2, false);
        var latestSubmission = submission(9001L, userId, lectureProblemSetId, 3001L, true, 1);

        given(learningLectureProblemSetPort.findLectureProblemSet(lectureProblemSetId))
                .willReturn(new LearningLectureProblemSet(lectureProblemSetId, 101L, problemSetId));
        given(learningProblemPort.loadProblemSet(problemSetId)).willReturn(problemSet(problemSetId));
        given(lectureProblemProgressRepository.findByUserIdAndLectureProblemSetId(userId, lectureProblemSetId))
                .willReturn(Optional.of(progress));
        given(lectureProblemSubmissionRepository.findByUserIdAndLectureProblemSetId(userId, lectureProblemSetId))
                .willReturn(List.of(latestSubmission));

        var result = lectureProblemSetService.enterLectureProblemSet(userId, lectureProblemSetId);

        assertEquals(2, result.currentProblemNumber());
        assertEquals(3002L, result.currentProblemId());
        assertEquals(1, result.solvedProblemCount());
        assertEquals("CORRECT", result.problems().get(0).status());
        assertEquals(9001L, result.problems().get(0).latestSubmissionId());
        assertEquals("UNSOLVED", result.problems().get(1).status());
    }

    @Test
    void submit_correctAnswer_savesLectureSubmissionAndAdvancesLectureProgress() {
        Long userId = 10L;
        Long lectureProblemSetId = 6001L;
        Long problemSetId = 2001L;
        Long problemId = 3001L;
        var currentProgress = progress(userId, lectureProblemSetId, 1, false);

        given(learningLectureProblemSetPort.findLectureProblemSet(lectureProblemSetId))
                .willReturn(new LearningLectureProblemSet(lectureProblemSetId, 101L, problemSetId));
        given(learningProblemPort.existsProblem(problemId)).willReturn(true);
        given(learningProblemPort.existsProblemInSet(problemSetId, problemId)).willReturn(true);
        given(learningProblemPort.loadProblem(problemId)).willReturn(
                new LearningProblemPort.ProblemForLearning(
                        problemId,
                        problemSetId,
                        1,
                        "explanation",
                        10,
                        3,
                        true
                )
        );
        given(lectureProblemProgressRepository.findByUserIdAndLectureProblemSetId(userId, lectureProblemSetId))
                .willReturn(Optional.of(currentProgress));
        given(lectureProblemProgressRepository.findByUserIdAndLectureProblemSetIdForUpdate(
                userId,
                lectureProblemSetId
        )).willReturn(Optional.of(currentProgress));
        given(lectureProblemSubmissionRepository.countAttempts(userId, lectureProblemSetId, problemId))
                .willReturn(0);
        given(learningProblemGradingPort.grade(problemSetId, problemId, "answer"))
                .willReturn(new LearningProblemGradingPort.GradingResult(
                        true,
                        2,
                        2,
                        "SUCCESS",
                        null
                ));
        given(lectureProblemSubmissionRepository.save(any(LectureProblemSubmission.class)))
                .willAnswer(invocation -> {
                    LectureProblemSubmission submission = invocation.getArgument(0);
                    return new LectureProblemSubmission(
                            9001L,
                            submission.userId(),
                            submission.lectureProblemSetId(),
                            submission.problemId(),
                            submission.submittedCode(),
                            submission.correct(),
                            submission.attemptNo(),
                            submission.passedTestCount(),
                            submission.totalTestCount(),
                            submission.executionStatus(),
                            submission.errorMessage(),
                            LocalDateTime.now()
                    );
                });
        given(learningProblemPort.loadProblemSet(problemSetId)).willReturn(problemSet(problemSetId));
        given(lectureProblemProgressCommandUseCase.recordProgress(any()))
                .willReturn(progress(userId, lectureProblemSetId, 2, false));

        var result = lectureProblemSetService.submit(
                lectureProblemSetId,
                problemId,
                new SubmitCodeCommand(userId, "answer")
        );

        assertEquals(9001L, result.submissionId());
        assertEquals(3002L, result.nextProblemId());
        assertFalse(result.problemSetCompleted());
        assertEquals(0, result.earnedPoint());
        assertFalse(result.pointGranted());

        ArgumentCaptor<LectureProblemSubmission> submissionCaptor =
                ArgumentCaptor.forClass(LectureProblemSubmission.class);
        verify(lectureProblemSubmissionRepository).save(submissionCaptor.capture());
        assertEquals(lectureProblemSetId, submissionCaptor.getValue().lectureProblemSetId());
        assertEquals(problemId, submissionCaptor.getValue().problemId());
        verify(lectureProblemProgressCommandUseCase).recordProgress(any());
    }

    @Test
    void submit_wrongAnswer_doesNotAdvanceLectureProgress() {
        Long userId = 10L;
        Long lectureProblemSetId = 6001L;
        Long problemSetId = 2001L;
        Long problemId = 3001L;
        var currentProgress = progress(userId, lectureProblemSetId, 1, false);
        givenSubmitContext(userId, lectureProblemSetId, problemSetId, problemId, currentProgress, 0, 3);
        given(learningProblemGradingPort.grade(problemSetId, problemId, "wrong"))
                .willReturn(new LearningProblemGradingPort.GradingResult(
                        false,
                        1,
                        2,
                        "WRONG_ANSWER",
                        "failed"
                ));
        given(lectureProblemSubmissionRepository.save(any(LectureProblemSubmission.class)))
                .willReturn(submission(9001L, userId, lectureProblemSetId, problemId, false, 1));

        var result = lectureProblemSetService.submit(
                lectureProblemSetId,
                problemId,
                new SubmitCodeCommand(userId, "wrong")
        );

        assertFalse(result.correct());
        assertEquals(2, result.remainingAttemptCount());
        assertEquals(null, result.nextProblemId());
        verify(lectureProblemProgressCommandUseCase, never()).recordProgress(any());
    }

    @Test
    void submit_attemptLimitExceeded_doesNotGradeOrSave() {
        Long userId = 10L;
        Long lectureProblemSetId = 6001L;
        Long problemSetId = 2001L;
        Long problemId = 3001L;
        var currentProgress = progress(userId, lectureProblemSetId, 1, false);
        givenSubmitContext(userId, lectureProblemSetId, problemSetId, problemId, currentProgress, 3, 3);

        assertThrows(
                com.wanted.codebombalms.global.domain.common.error.exception.ValidationException.class,
                () -> lectureProblemSetService.submit(
                        lectureProblemSetId,
                        problemId,
                        new SubmitCodeCommand(userId, "answer")
                )
        );

        verify(learningProblemGradingPort, never()).grade(any(), any(), any());
        verify(lectureProblemSubmissionRepository, never()).save(any());
        verify(lectureProblemProgressCommandUseCase, never()).recordProgress(any());
    }

    @Test
    void submit_gradingFailure_doesNotSaveSubmissionOrProgress() {
        Long userId = 10L;
        Long lectureProblemSetId = 6001L;
        Long problemSetId = 2001L;
        Long problemId = 3001L;
        var currentProgress = progress(userId, lectureProblemSetId, 1, false);
        givenSubmitContext(userId, lectureProblemSetId, problemSetId, problemId, currentProgress, 0, 3);
        given(learningProblemGradingPort.grade(problemSetId, problemId, "answer"))
                .willThrow(new IllegalStateException("grading failed"));

        assertThrows(
                IllegalStateException.class,
                () -> lectureProblemSetService.submit(
                        lectureProblemSetId,
                        problemId,
                        new SubmitCodeCommand(userId, "answer")
                )
        );

        verify(lectureProblemSubmissionRepository, never()).save(any());
        verify(lectureProblemProgressCommandUseCase, never()).recordProgress(any());
    }

    private void givenSubmitContext(
            Long userId,
            Long lectureProblemSetId,
            Long problemSetId,
            Long problemId,
            LectureProblemProgress progress,
            int previousAttemptCount,
            int attemptLimit
    ) {
        given(learningLectureProblemSetPort.findLectureProblemSet(lectureProblemSetId))
                .willReturn(new LearningLectureProblemSet(lectureProblemSetId, 101L, problemSetId));
        given(learningProblemPort.existsProblem(problemId)).willReturn(true);
        given(learningProblemPort.existsProblemInSet(problemSetId, problemId)).willReturn(true);
        given(learningProblemPort.loadProblem(problemId)).willReturn(
                new LearningProblemPort.ProblemForLearning(
                        problemId,
                        problemSetId,
                        1,
                        "explanation",
                        10,
                        attemptLimit,
                        true
                )
        );
        given(lectureProblemProgressRepository.findByUserIdAndLectureProblemSetId(
                userId,
                lectureProblemSetId
        )).willReturn(Optional.of(progress));
        given(lectureProblemProgressRepository.findByUserIdAndLectureProblemSetIdForUpdate(
                userId,
                lectureProblemSetId
        )).willReturn(Optional.of(progress));
        given(lectureProblemSubmissionRepository.countAttempts(userId, lectureProblemSetId, problemId))
                .willReturn(previousAttemptCount);
    }

    private LearningProblemPort.ProblemSetForLearning problemSet(Long problemSetId) {
        return new LearningProblemPort.ProblemSetForLearning(
                problemSetId,
                "Lecture problem set",
                "Description",
                List.of(
                        new LearningProblemPort.ProblemDetailForLearning(
                                3001L,
                                1,
                                "Problem 1",
                                "Content 1",
                                "CODE",
                                10,
                                "start"
                        ),
                        new LearningProblemPort.ProblemDetailForLearning(
                                3002L,
                                2,
                                "Problem 2",
                                "Content 2",
                                "CODE",
                                10,
                                "start"
                        )
                )
        );
    }

    private LectureProblemProgress progress(
            Long userId,
            Long lectureProblemSetId,
            int currentProblemNumber,
            boolean completed
    ) {
        return LectureProblemProgress.restore(
                1L,
                userId,
                lectureProblemSetId,
                currentProblemNumber,
                completed,
                completed ? LocalDateTime.now() : null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private LectureProblemSubmission submission(
            Long submissionId,
            Long userId,
            Long lectureProblemSetId,
            Long problemId,
            boolean correct,
            int attemptNo
    ) {
        return new LectureProblemSubmission(
                submissionId,
                userId,
                lectureProblemSetId,
                problemId,
                "answer",
                correct,
                attemptNo,
                correct ? 2 : 1,
                2,
                correct ? "SUCCESS" : "WRONG_ANSWER",
                null,
                LocalDateTime.now()
        );
    }
}
