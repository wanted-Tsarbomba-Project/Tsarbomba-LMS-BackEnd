package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.learning.application.command.RecordLectureProgressCommand;
import com.wanted.codebombalms.learning.application.command.SubmitLectureProblemCommand;
import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import com.wanted.codebombalms.learning.application.port.LearningEnrollmentPort;
import com.wanted.codebombalms.learning.application.port.LearningLecturePort;
import com.wanted.codebombalms.learning.application.port.LearningProblemPort;
import com.wanted.codebombalms.learning.application.port.LearningUserPort;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemSubmissionUseCase.LectureProblemSubmissionResult;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;
import com.wanted.codebombalms.learning.domain.model.LectureProgress;
import com.wanted.codebombalms.learning.domain.model.StudentLearningProgress;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemSubmissionRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository;
import com.wanted.codebombalms.submission.application.policy.SubmissionAnswerPolicy;
import com.wanted.codebombalms.submission.application.policy.SubmissionAttemptPolicy;
import com.wanted.codebombalms.submission.application.service.AnswerGradingService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Learning application service unit test")
class LearningServiceTest {

    @Mock
    private LectureProgressRepository lectureProgressRepository;

    @Mock
    private LectureProblemSubmissionRepository lectureProblemSubmissionRepository;

    @Mock
    private LectureProblemProgressRepository lectureProblemProgressRepository;

    @Mock
    private LearningLecturePort learningLecturePort;

    @Mock
    private LearningCourseProblemPort learningCourseProblemPort;

    @Mock
    private LearningProblemPort learningProblemPort;

    @Mock
    private LearningEnrollmentPort learningEnrollmentPort;

    @Mock
    private LearningUserPort learningUserPort;

    @Spy
    private SubmissionAnswerPolicy submissionAnswerPolicy;

    @Spy
    private SubmissionAttemptPolicy submissionAttemptPolicy;

    @Spy
    private AnswerGradingService answerGradingService;

    @InjectMocks
    private LectureProgressService lectureProgressService;

    @InjectMocks
    private LectureProblemSubmissionService lectureProblemSubmissionService;

    @InjectMocks
    private AdminLearningProgressQueryService adminLearningProgressQueryService;

    @Test
    void recordProgress_completesLectureProgress() {
        Long userId = 10L;
        Long lectureId = 101L;
        LectureProgress savedProgress = LectureProgress.restore(
                1L,
                userId,
                lectureId,
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);
        given(lectureProgressRepository.findByUserIdAndLectureId(userId, lectureId)).willReturn(Optional.empty());
        given(lectureProgressRepository.save(any(LectureProgress.class))).willReturn(savedProgress);

        LectureProgress result = lectureProgressService.recordProgress(
                new RecordLectureProgressCommand(userId, lectureId, true)
        );

        assertEquals(1L, result.getLectureProgressId());
        assertTrue(result.isCompleted());
        assertNotNull(result.getCompletedAt());
        verify(lectureProgressRepository).save(any(LectureProgress.class));
    }

    @Test
    void findProgress_throwsNotFound_whenLectureMissing() {
        Long lectureId = 999L;
        given(learningLecturePort.existsLecture(lectureId)).willReturn(false);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> lectureProgressService.findProgress(10L, lectureId)
        );

        assertEquals(LearningErrorCode.LECTURE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void submit_correctAnswer_savesSubmissionAndCompletesProgress() {
        Long userId = 10L;
        Long courseProblemStepId = 6001L;
        Long problemId = 2004L;

        given(learningCourseProblemPort.findCourseProblemStep(courseProblemStepId))
                .willReturn(Optional.of(new LearningCourseProblemPort.CourseProblemStepInfo(
                        courseProblemStepId,
                        problemId,
                        101L
                )));
        given(learningProblemPort.loadProblem(problemId))
                .willReturn(new LearningProblemPort.ProblemForLearning(
                        problemId,
                        "정답",
                        "풀이 설명",
                        3,
                        true
                ));
        given(lectureProblemSubmissionRepository.countAttempts(userId, courseProblemStepId)).willReturn(0);
        given(lectureProblemSubmissionRepository.save(any(LectureProblemSubmission.class)))
                .willReturn(new LectureProblemSubmission(
                        1L,
                        userId,
                        courseProblemStepId,
                        problemId,
                        "정답",
                        true,
                        1,
                        LocalDateTime.now()
                ));
        given(lectureProblemProgressRepository.findByUserIdAndCourseProblemStepId(userId, courseProblemStepId))
                .willReturn(Optional.empty());
        given(lectureProblemProgressRepository.save(any(LectureProblemProgress.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        LectureProblemSubmissionResult result = lectureProblemSubmissionService.submit(
                new SubmitLectureProblemCommand(userId, courseProblemStepId, "정답")
        );

        assertEquals(1L, result.lectureProblemSubmissionId());
        assertTrue(result.correct());
        assertEquals(1, result.attemptNo());
        assertEquals(2, result.remainingAttemptCount());
        assertFalse(result.canRetry());
        assertTrue(result.completed());
        assertEquals("풀이 설명", result.explanation());
        verify(lectureProblemSubmissionRepository).save(any(LectureProblemSubmission.class));
        verify(lectureProblemProgressRepository).save(any(LectureProblemProgress.class));
    }

    @Test
    void submit_wrongAnswer_returnsRetryableResult() {
        Long userId = 10L;
        Long courseProblemStepId = 6001L;
        Long problemId = 2004L;

        given(learningCourseProblemPort.findCourseProblemStep(courseProblemStepId))
                .willReturn(Optional.of(new LearningCourseProblemPort.CourseProblemStepInfo(
                        courseProblemStepId,
                        problemId,
                        101L
                )));
        given(learningProblemPort.loadProblem(problemId))
                .willReturn(new LearningProblemPort.ProblemForLearning(
                        problemId,
                        "정답",
                        "풀이 설명",
                        3,
                        true
                ));
        given(lectureProblemSubmissionRepository.countAttempts(userId, courseProblemStepId)).willReturn(1);
        given(lectureProblemSubmissionRepository.save(any(LectureProblemSubmission.class)))
                .willReturn(new LectureProblemSubmission(
                        2L,
                        userId,
                        courseProblemStepId,
                        problemId,
                        "오답",
                        false,
                        0,
                        LocalDateTime.now()
                ));
        given(lectureProblemProgressRepository.findByUserIdAndCourseProblemStepId(userId, courseProblemStepId))
                .willReturn(Optional.of(LectureProblemProgress.create(userId, courseProblemStepId)));
        given(lectureProblemProgressRepository.save(any(LectureProblemProgress.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        LectureProblemSubmissionResult result = lectureProblemSubmissionService.submit(
                new SubmitLectureProblemCommand(userId, courseProblemStepId, "오답")
        );

        assertFalse(result.correct());
        assertEquals(2, result.attemptNo());
        assertEquals(1, result.remainingAttemptCount());
        assertTrue(result.canRetry());
        assertFalse(result.completed());
        assertNull(result.explanation());
    }

    @Test
    void findStudentProgresses_returnsAggregatedProgress() {
        Long courseId = 101L;
        Long userId = 10L;
        List<Long> lectureIds = List.of(101L, 102L);
        List<Long> courseProblemStepIds = List.of(6001L, 6002L, 6003L);

        given(learningEnrollmentPort.findActiveStudentIdsByCourse(courseId)).willReturn(List.of(userId));
        given(learningLecturePort.findLectureIdsByCourse(courseId)).willReturn(lectureIds);
        given(learningCourseProblemPort.findCourseProblemStepIdsByCourse(courseId)).willReturn(courseProblemStepIds);
        given(learningUserPort.findUserName(userId)).willReturn("학생");
        given(lectureProgressRepository.countCompletedByUserIdAndLectureIds(userId, lectureIds)).willReturn(1L);
        given(lectureProblemProgressRepository.countCompletedByUserIdAndCourseProblemStepIds(
                userId,
                courseProblemStepIds
        )).willReturn(2L);

        List<StudentLearningProgress> results = adminLearningProgressQueryService.findStudentProgresses(courseId);

        assertEquals(1, results.size());
        StudentLearningProgress result = results.get(0);
        assertEquals(userId, result.userId());
        assertEquals("학생", result.studentName());
        assertEquals(1L, result.completedLectureCount());
        assertEquals(2L, result.totalLectureCount());
        assertEquals(50, result.lectureProgressRate());
        assertEquals(2L, result.completedProblemCount());
        assertEquals(3L, result.totalProblemCount());
    }
}
