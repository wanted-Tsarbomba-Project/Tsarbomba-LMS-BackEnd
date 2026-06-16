package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.learning.application.command.RecordLectureProgressCommand;
import com.wanted.codebombalms.learning.application.port.LearningCoursePort;
import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import com.wanted.codebombalms.learning.application.port.LearningEnrollmentPort;
import com.wanted.codebombalms.learning.application.port.LearningLecture;
import com.wanted.codebombalms.learning.application.port.LearningLecturePort;
import com.wanted.codebombalms.learning.application.port.LearningUserPort;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.learning.domain.model.CourseLearningProgress;
import com.wanted.codebombalms.learning.domain.model.LearningCourse;
import com.wanted.codebombalms.learning.domain.model.LearningProgressSummary;
import com.wanted.codebombalms.learning.domain.model.LectureLearningProgress;
import com.wanted.codebombalms.learning.domain.model.LectureProblemStatistics;
import com.wanted.codebombalms.learning.domain.model.LectureProgress;
import com.wanted.codebombalms.learning.domain.model.StudentLearningProgress;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private LectureProblemProgressRepository lectureProblemProgressRepository;

    @Mock
    private LearningCoursePort learningCoursePort;

    @Mock
    private LearningLecturePort learningLecturePort;

    @Mock
    private LearningCourseProblemPort learningCourseProblemPort;

    @Mock
    private LearningEnrollmentPort learningEnrollmentPort;

    @Mock
    private LearningUserPort learningUserPort;

    @InjectMocks
    private LectureProgressService lectureProgressService;

    @InjectMocks
    private AdminLearningProgressQueryService adminLearningProgressQueryService;

    @Test
    void recordProgress_completesLectureProgress() {
        Long userId = 10L;
        Long lectureId = 101L;
        LectureProgress existingProgress = LectureProgress.restore(
                1L,
                userId,
                lectureId,
                false,
                null,
                LocalDateTime.now(),
                530,
                600,
                530,
                LocalDateTime.now(),
                null
        );

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);
        given(lectureProgressRepository.findByUserIdAndLectureId(userId, lectureId))
                .willReturn(Optional.of(existingProgress));
        given(lectureProgressRepository.save(any(LectureProgress.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        LectureProgress result = lectureProgressService.recordProgress(
                new RecordLectureProgressCommand(userId, lectureId, 540, 600, 10)
        );

        assertEquals(1L, result.getLectureProgressId());
        assertTrue(result.isCompleted());
        assertNotNull(result.getCompletedAt());
        assertEquals(540, result.getLastPositionSec());
        assertEquals(540, result.getWatchedSec());
        verify(lectureProgressRepository).save(any(LectureProgress.class));
    }

    @Test
    void recordProgress_doesNotComplete_whenOnlyPositionReachesThreshold() {
        Long userId = 10L;
        Long lectureId = 101L;
        LectureProgress existingProgress = LectureProgress.restore(
                1L,
                userId,
                lectureId,
                false,
                null,
                LocalDateTime.now(),
                300,
                600,
                300,
                LocalDateTime.now(),
                null
        );

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);
        given(lectureProgressRepository.findByUserIdAndLectureId(userId, lectureId))
                .willReturn(Optional.of(existingProgress));
        given(lectureProgressRepository.save(any(LectureProgress.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        LectureProgress result = lectureProgressService.recordProgress(
                new RecordLectureProgressCommand(userId, lectureId, 540, 600, 10)
        );

        assertFalse(result.isCompleted());
        assertEquals(540, result.getLastPositionSec());
        assertEquals(310, result.getWatchedSec());
    }

    @Test
    void recordProgress_doesNotComplete_whenOnlyWatchedSecondsReachThreshold() {
        Long userId = 10L;
        Long lectureId = 101L;
        LectureProgress existingProgress = LectureProgress.restore(
                1L,
                userId,
                lectureId,
                false,
                null,
                LocalDateTime.now(),
                300,
                600,
                530,
                LocalDateTime.now(),
                null
        );

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);
        given(lectureProgressRepository.findByUserIdAndLectureId(userId, lectureId))
                .willReturn(Optional.of(existingProgress));
        given(lectureProgressRepository.save(any(LectureProgress.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        LectureProgress result = lectureProgressService.recordProgress(
                new RecordLectureProgressCommand(userId, lectureId, 400, 600, 10)
        );

        assertFalse(result.isCompleted());
        assertEquals(400, result.getLastPositionSec());
        assertEquals(540, result.getWatchedSec());
    }

    @Test
    void recordProgress_keepsCompleted_whenCompletedLectureIsRewatched() {
        Long userId = 10L;
        Long lectureId = 101L;
        LocalDateTime completedAt = LocalDateTime.now().minusDays(1);
        LectureProgress existingProgress = LectureProgress.restore(
                1L,
                userId,
                lectureId,
                true,
                completedAt,
                LocalDateTime.now().minusDays(1),
                540,
                600,
                540,
                LocalDateTime.now().minusDays(2),
                null
        );

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);
        given(lectureProgressRepository.findByUserIdAndLectureId(userId, lectureId))
                .willReturn(Optional.of(existingProgress));
        given(lectureProgressRepository.save(any(LectureProgress.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        LectureProgress result = lectureProgressService.recordProgress(
                new RecordLectureProgressCommand(userId, lectureId, 120, 600, 10)
        );

        assertTrue(result.isCompleted());
        assertEquals(completedAt, result.getCompletedAt());
        assertEquals(120, result.getLastPositionSec());
        assertEquals(550, result.getWatchedSec());
    }

    @Test
    void recordProgress_throwsValidation_whenWatchedDeltaIsNegative() {
        Long userId = 10L;
        Long lectureId = 101L;

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureProgressService.recordProgress(
                        new RecordLectureProgressCommand(userId, lectureId, 120, 600, -1)
                )
        );

        assertEquals(LearningErrorCode.INVALID_LECTURE_PROGRESS, exception.getErrorCode());
    }

    @Test
    void recordProgress_throwsValidation_whenLastPositionExceedsRequestedDuration() {
        Long userId = 10L;
        Long lectureId = 101L;

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureProgressService.recordProgress(
                        new RecordLectureProgressCommand(userId, lectureId, 601, 600, 10)
                )
        );

        assertEquals(LearningErrorCode.INVALID_LECTURE_PROGRESS, exception.getErrorCode());
    }

    @Test
    void recordProgress_throwsValidation_whenLastPositionExceedsSavedDuration() {
        Long userId = 10L;
        Long lectureId = 101L;
        LectureProgress existingProgress = LectureProgress.restore(
                1L,
                userId,
                lectureId,
                false,
                null,
                LocalDateTime.now(),
                100,
                600,
                100,
                LocalDateTime.now(),
                null
        );

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);
        given(lectureProgressRepository.findByUserIdAndLectureId(userId, lectureId))
                .willReturn(Optional.of(existingProgress));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureProgressService.recordProgress(
                        new RecordLectureProgressCommand(userId, lectureId, 601, null, 10)
                )
        );

        assertEquals(LearningErrorCode.INVALID_LECTURE_PROGRESS, exception.getErrorCode());
    }

    @Test
    void recordProgress_throwsValidation_whenRequestedDurationDiffersFromSavedDuration() {
        Long userId = 10L;
        Long lectureId = 101L;
        LectureProgress existingProgress = LectureProgress.restore(
                1L,
                userId,
                lectureId,
                false,
                null,
                LocalDateTime.now(),
                100,
                600,
                100,
                LocalDateTime.now(),
                null
        );

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);
        given(lectureProgressRepository.findByUserIdAndLectureId(userId, lectureId))
                .willReturn(Optional.of(existingProgress));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureProgressService.recordProgress(
                        new RecordLectureProgressCommand(userId, lectureId, 120, 300, 10)
                )
        );

        assertEquals(LearningErrorCode.INVALID_LECTURE_PROGRESS, exception.getErrorCode());
    }

    @Test
    void completeProgress_completesLectureProgress() {
        Long userId = 10L;
        Long lectureId = 101L;

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);
        given(lectureProgressRepository.findByUserIdAndLectureId(userId, lectureId)).willReturn(Optional.empty());
        given(lectureProgressRepository.save(any(LectureProgress.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        LectureProgress result = lectureProgressService.completeProgress(userId, lectureId);

        assertTrue(result.isCompleted());
        assertNotNull(result.getCompletedAt());
        verify(lectureProgressRepository).save(any(LectureProgress.class));
    }

    @Test
    void completeProgress_keepsCompletedAt_whenAlreadyCompleted() {
        Long userId = 10L;
        Long lectureId = 101L;
        LocalDateTime completedAt = LocalDateTime.now().minusDays(1);
        LectureProgress existingProgress = LectureProgress.restore(
                1L,
                userId,
                lectureId,
                true,
                completedAt,
                LocalDateTime.now().minusDays(1),
                0,
                null,
                0,
                LocalDateTime.now().minusDays(2),
                null
        );

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);
        given(lectureProgressRepository.findByUserIdAndLectureId(userId, lectureId))
                .willReturn(Optional.of(existingProgress));
        given(lectureProgressRepository.save(any(LectureProgress.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        LectureProgress result = lectureProgressService.completeProgress(userId, lectureId);

        assertTrue(result.isCompleted());
        assertEquals(completedAt, result.getCompletedAt());
        verify(lectureProgressRepository).save(any(LectureProgress.class));
    }

    @Test
    void completeProgress_throwsNotFound_whenLectureMissing() {
        Long lectureId = 999L;
        given(learningLecturePort.existsLecture(lectureId)).willReturn(false);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> lectureProgressService.completeProgress(10L, lectureId)
        );

        assertEquals(LearningErrorCode.LECTURE_NOT_FOUND, exception.getErrorCode());
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
    void findStudentProgresses_returnsAggregatedProgress() {
        Long courseId = 101L;
        Long userId = 10L;
        List<Long> lectureIds = List.of(101L, 102L);
        List<Long> lectureProblemSetIds = List.of(6001L, 6002L, 6003L);

        given(learningEnrollmentPort.findActiveStudentIdsByCourse(courseId)).willReturn(List.of(userId));
        given(learningLecturePort.findLectureIdsByCourse(courseId)).willReturn(lectureIds);
        given(learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(courseId)).willReturn(lectureProblemSetIds);
        given(learningUserPort.findUserName(userId)).willReturn("학생");
        given(lectureProgressRepository.countCompletedByUserIdAndLectureIds(userId, lectureIds)).willReturn(1L);
        given(lectureProblemProgressRepository.countCompletedByUserIdAndLectureProblemSetIds(
                userId,
                lectureProblemSetIds
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

    @Test
    void findCourseProgress_returnsAggregatedProgress() {
        Long courseId = 101L;
        Long userId = 10L;
        List<Long> lectureIds = List.of(101L, 102L);
        List<Long> lectureProblemSetIds = List.of(6001L, 6002L, 6003L);

        given(learningCoursePort.findActiveCourse(courseId)).willReturn(new LearningCourse(courseId, "Java"));
        given(learningEnrollmentPort.findActiveStudentIdsByCourse(courseId)).willReturn(List.of(userId));
        given(learningLecturePort.findLectureIdsByCourse(courseId)).willReturn(lectureIds);
        given(learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(courseId)).willReturn(lectureProblemSetIds);
        given(learningUserPort.findUserName(userId)).willReturn("?숈깮");
        given(lectureProgressRepository.countCompletedByUserIdAndLectureIds(userId, lectureIds)).willReturn(1L);
        given(lectureProblemProgressRepository.countCompletedByUserIdAndLectureProblemSetIds(
                userId,
                lectureProblemSetIds
        )).willReturn(2L);

        CourseLearningProgress result = adminLearningProgressQueryService.findCourseProgress(courseId);

        assertEquals(courseId, result.courseId());
        assertEquals("Java", result.courseTitle());
        assertEquals(1L, result.enrolledStudentCount());
        assertEquals(1L, result.completedLectureCount());
        assertEquals(2L, result.totalLectureCount());
        assertEquals(50, result.averageLectureProgressRate());
    }

    @Test
    void findLectureProgresses_returnsProgressByLecture() {
        Long courseId = 101L;
        given(learningEnrollmentPort.findActiveStudentIdsByCourse(courseId)).willReturn(List.of(10L, 11L));
        given(learningLecturePort.findLecturesByCourse(courseId))
                .willReturn(List.of(new LearningLecture(201L, "OT")));
        given(lectureProgressRepository.countCompletedByLectureId(201L)).willReturn(1L);

        List<LectureLearningProgress> results = adminLearningProgressQueryService.findLectureProgresses(courseId);

        assertEquals(1, results.size());
        assertEquals(201L, results.get(0).lectureId());
        assertEquals(50, results.get(0).progressRate());
    }

    @Test
    void findLectureProblemStatistics_returnsCompletionStatistics() {
        Long lectureId = 201L;
        Long courseId = 101L;
        List<Long> lectureProblemSetIds = List.of(6001L, 6002L, 6003L);
        given(learningCourseProblemPort.findLectureProblemSetIdsByLecture(lectureId)).willReturn(lectureProblemSetIds);
        given(learningLecturePort.findCourseIdByLecture(lectureId)).willReturn(courseId);
        given(learningEnrollmentPort.findActiveStudentIdsByCourse(courseId)).willReturn(List.of(10L, 11L));
        given(lectureProblemProgressRepository.countCompletedByLectureProblemSetIds(lectureProblemSetIds))
                .willReturn(2L);

        LectureProblemStatistics result = adminLearningProgressQueryService.findLectureProblemStatistics(lectureId);

        assertEquals(lectureId, result.lectureId());
        assertEquals(2L, result.completedProblemSetCount());
        assertEquals(6L, result.totalProblemSetCount());
        assertEquals(33, result.completionRate());
    }

    @Test
    void summarizeLearningProgress_returnsSummary() {
        Long courseId = 101L;
        Long userId = 10L;
        List<Long> lectureIds = List.of(101L, 102L);
        List<Long> lectureProblemSetIds = List.of(6001L, 6002L);

        given(learningCoursePort.findActiveCourses()).willReturn(List.of(new LearningCourse(courseId, "Java")));
        given(learningEnrollmentPort.findActiveStudentIdsByCourse(courseId)).willReturn(List.of(userId));
        given(learningLecturePort.findLectureIdsByCourse(courseId)).willReturn(lectureIds);
        given(learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(courseId)).willReturn(lectureProblemSetIds);
        given(learningUserPort.findUserName(userId)).willReturn("?숈깮");
        given(lectureProgressRepository.countCompletedByUserIdAndLectureIds(userId, lectureIds)).willReturn(1L);
        given(lectureProblemProgressRepository.countCompletedByUserIdAndLectureProblemSetIds(
                userId,
                lectureProblemSetIds
        )).willReturn(2L);

        LearningProgressSummary result = adminLearningProgressQueryService.summarizeLearningProgress();

        assertEquals(1L, result.totalCourseCount());
        assertEquals(1L, result.totalStudentCount());
        assertEquals(1L, result.completedLectureCount());
        assertEquals(2L, result.totalLectureCount());
        assertEquals(50, result.averageLectureProgressRate());
        assertEquals(2L, result.completedProblemCount());
    }
}
