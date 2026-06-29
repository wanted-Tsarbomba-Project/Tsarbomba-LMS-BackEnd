package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.learning.application.command.RecordLectureProgressCommand;
import com.wanted.codebombalms.learning.application.port.LearningCoursePort;
import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import com.wanted.codebombalms.learning.application.port.LearningEnrollmentPort;
import com.wanted.codebombalms.learning.application.port.LearningLecture;
import com.wanted.codebombalms.learning.application.port.LearningLecturePort;
import com.wanted.codebombalms.learning.application.port.LearningProgressMetricsPort;
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
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @Mock
    private LearningEnrollmentCacheService learningEnrollmentCacheService;

    @Mock
    private LearningCourseStructureCacheService learningCourseStructureCacheService;

    @Mock
    private LearningProgressCountCacheService learningProgressCountCacheService;

    @Mock
    private LearningProgressMetricsPort learningMetrics;

    @InjectMocks
    private LectureProgressService lectureProgressService;

    @InjectMocks
    private AdminLearningProgressQueryService adminLearningProgressQueryService;

    @BeforeEach
    void setUp() {
        lenient().when(learningLecturePort.findCourseIdByLecture(anyLong())).thenReturn(1L);
        lenient().when(learningEnrollmentPort.isActiveStudentOfCourse(anyLong(), anyLong())).thenReturn(true);
        lenient().when(learningEnrollmentCacheService.countActiveStudentsByCourse(anyLong()))
                .thenAnswer(invocation -> learningEnrollmentPort.countActiveStudentsByCourse(invocation.getArgument(0)));
        lenient().when(learningEnrollmentCacheService.findActiveStudentIdsByCourse(anyLong(), anyInt(), anyInt()))
                .thenAnswer(invocation -> learningEnrollmentPort.findActiveStudentIdsByCourse(
                        invocation.getArgument(0),
                        invocation.getArgument(1),
                        invocation.getArgument(2)
                ));
        lenient().when(learningCourseStructureCacheService.findLectureIdsByCourse(anyLong()))
                .thenAnswer(invocation -> learningLecturePort.findLectureIdsByCourse(invocation.getArgument(0)));
        lenient().when(learningCourseStructureCacheService.findMainLectureProblemSetIdsByCourse(anyLong()))
                .thenAnswer(invocation -> learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(
                        invocation.getArgument(0)
                ));
        lenient().when(learningProgressCountCacheService.countCompletedLectures(anyLong(), any(), any()))
                .thenAnswer(invocation -> lectureProgressRepository.countCompletedByUserIdsAndLectureIds(
                        invocation.getArgument(1),
                        invocation.getArgument(2)
                ));
        lenient().when(learningProgressCountCacheService.countCompletedProblems(anyLong(), any(), any()))
                .thenAnswer(invocation -> lectureProblemProgressRepository.countCompletedByUserIdsAndLectureProblemSetIds(
                        invocation.getArgument(1),
                        invocation.getArgument(2)
                ));
    }

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
    void recordProgress_throwsForbidden_whenStudentIsNotEnrolled() {
        Long userId = 10L;
        Long lectureId = 101L;
        Long courseId = 1L;

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);
        given(learningLecturePort.findCourseIdByLecture(lectureId)).willReturn(courseId);
        given(learningEnrollmentPort.isActiveStudentOfCourse(courseId, userId)).willReturn(false);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> lectureProgressService.recordProgress(
                        new RecordLectureProgressCommand(userId, lectureId, 120, 600, 10)
                )
        );

        assertEquals(LearningErrorCode.LECTURE_PROGRESS_ACCESS_DENIED, exception.getErrorCode());
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
    void completeProgress_throwsForbidden_whenStudentIsNotEnrolled() {
        Long userId = 10L;
        Long lectureId = 101L;
        Long courseId = 1L;

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);
        given(learningLecturePort.findCourseIdByLecture(lectureId)).willReturn(courseId);
        given(learningEnrollmentPort.isActiveStudentOfCourse(courseId, userId)).willReturn(false);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> lectureProgressService.completeProgress(userId, lectureId)
        );

        assertEquals(LearningErrorCode.LECTURE_PROGRESS_ACCESS_DENIED, exception.getErrorCode());
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
    void findProgress_throwsForbidden_whenStudentIsNotEnrolled() {
        Long userId = 10L;
        Long lectureId = 101L;
        Long courseId = 1L;

        given(learningLecturePort.existsLecture(lectureId)).willReturn(true);
        given(learningLecturePort.findCourseIdByLecture(lectureId)).willReturn(courseId);
        given(learningEnrollmentPort.isActiveStudentOfCourse(courseId, userId)).willReturn(false);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> lectureProgressService.findProgress(userId, lectureId)
        );

        assertEquals(LearningErrorCode.LECTURE_PROGRESS_ACCESS_DENIED, exception.getErrorCode());
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
        List<Long> userIds = List.of(userId);
        given(learningUserPort.findUserNames(userIds)).willReturn(Map.of(userId, "학생"));
        given(lectureProgressRepository.countCompletedByUserIdsAndLectureIds(userIds, lectureIds)).willReturn(Map.of(userId, 1L));
        given(lectureProblemProgressRepository.countCompletedByUserIdsAndLectureProblemSetIds(
                userIds,
                lectureProblemSetIds
        )).willReturn(Map.of(userId, 2L));

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
        verify(learningMetrics).recordStudentProgressQuery(anyLong());
        verify(learningMetrics, times(1)).recordStudentProgressItem(anyLong());
    }

    @Test
    void findStudentProgresses_treatsMissingBulkCountsAsZero() {
        Long courseId = 101L;
        Long firstUserId = 10L;
        Long secondUserId = 11L;
        List<Long> lectureIds = List.of(101L, 102L);
        List<Long> lectureProblemSetIds = List.of(6001L, 6002L, 6003L);
        List<Long> userIds = List.of(firstUserId, secondUserId);

        given(learningEnrollmentPort.findActiveStudentIdsByCourse(courseId)).willReturn(userIds);
        given(learningLecturePort.findLectureIdsByCourse(courseId)).willReturn(lectureIds);
        given(learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(courseId)).willReturn(lectureProblemSetIds);
        given(learningUserPort.findUserNames(userIds))
                .willReturn(Map.of(firstUserId, "student1", secondUserId, "student2"));
        given(lectureProgressRepository.countCompletedByUserIdsAndLectureIds(userIds, lectureIds))
                .willReturn(Map.of(firstUserId, 1L));
        given(lectureProblemProgressRepository.countCompletedByUserIdsAndLectureProblemSetIds(
                userIds,
                lectureProblemSetIds
        )).willReturn(Map.of(secondUserId, 2L));

        List<StudentLearningProgress> results = adminLearningProgressQueryService.findStudentProgresses(courseId);

        StudentLearningProgress first = results.get(0);
        StudentLearningProgress second = results.get(1);
        assertEquals(1L, first.completedLectureCount());
        assertEquals(0L, first.completedProblemCount());
        assertEquals(50, first.lectureProgressRate());
        assertEquals(0L, second.completedLectureCount());
        assertEquals(2L, second.completedProblemCount());
        assertEquals(0, second.lectureProgressRate());
    }

    @Test
    void findStudentProgresses_reusesCourseItemIdsForMultipleStudents() {
        Long courseId = 101L;
        Long firstUserId = 10L;
        Long secondUserId = 11L;
        List<Long> lectureIds = List.of(101L, 102L);
        List<Long> lectureProblemSetIds = List.of(6001L, 6002L, 6003L);

        given(learningEnrollmentPort.findActiveStudentIdsByCourse(courseId))
                .willReturn(List.of(firstUserId, secondUserId));
        given(learningLecturePort.findLectureIdsByCourse(courseId)).willReturn(lectureIds);
        given(learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(courseId)).willReturn(lectureProblemSetIds);
        List<Long> userIds = List.of(firstUserId, secondUserId);
        given(learningUserPort.findUserNames(userIds))
                .willReturn(Map.of(firstUserId, "student1", secondUserId, "student2"));
        given(lectureProgressRepository.countCompletedByUserIdsAndLectureIds(userIds, lectureIds))
                .willReturn(Map.of(firstUserId, 1L, secondUserId, 2L));
        given(lectureProblemProgressRepository.countCompletedByUserIdsAndLectureProblemSetIds(
                userIds,
                lectureProblemSetIds
        )).willReturn(Map.of(firstUserId, 2L, secondUserId, 3L));

        List<StudentLearningProgress> results = adminLearningProgressQueryService.findStudentProgresses(courseId);

        assertEquals(2, results.size());
        verify(learningLecturePort, times(1)).findLectureIdsByCourse(courseId);
        verify(learningCourseProblemPort, times(1)).findMainLectureProblemSetIdsByCourse(courseId);
        verify(learningMetrics, times(2)).recordStudentProgressItem(anyLong());
        verify(learningMetrics).recordStudentProgressQuery(anyLong());
    }

    @Test
    void findStudentProgresses_withPage_returnsPagedProgress() {
        Long courseId = 101L;
        Long userId = 10L;
        List<Long> lectureIds = List.of(101L, 102L);
        List<Long> lectureProblemSetIds = List.of(6001L, 6002L, 6003L);
        List<Long> userIds = List.of(userId);

        doReturn(21L).when(learningEnrollmentCacheService).countActiveStudentsByCourse(courseId);
        doReturn(userIds).when(learningEnrollmentCacheService).findActiveStudentIdsByCourse(courseId, 1, 20);
        doReturn(lectureIds).when(learningCourseStructureCacheService).findLectureIdsByCourse(courseId);
        doReturn(lectureProblemSetIds)
                .when(learningCourseStructureCacheService)
                .findMainLectureProblemSetIdsByCourse(courseId);
        given(learningUserPort.findUserNames(userIds)).willReturn(Map.of(userId, "student1"));
        doReturn(Map.of(userId, 1L))
                .when(learningProgressCountCacheService)
                .countCompletedLectures(courseId, userIds, lectureIds);
        doReturn(Map.of(userId, 2L))
                .when(learningProgressCountCacheService)
                .countCompletedProblems(courseId, userIds, lectureProblemSetIds);

        var result = adminLearningProgressQueryService.findStudentProgresses(courseId, 1);

        assertEquals(1, result.content().size());
        assertEquals(1, result.page());
        assertEquals(20, result.size());
        assertEquals(21L, result.totalElements());
        assertEquals(2, result.totalPages());
        assertFalse(result.hasNext());
        verify(learningEnrollmentCacheService).countActiveStudentsByCourse(courseId);
        verify(learningEnrollmentCacheService).findActiveStudentIdsByCourse(courseId, 1, 20);
        verify(learningCourseStructureCacheService).findLectureIdsByCourse(courseId);
        verify(learningCourseStructureCacheService).findMainLectureProblemSetIdsByCourse(courseId);
        verify(learningProgressCountCacheService).countCompletedLectures(courseId, userIds, lectureIds);
        verify(learningProgressCountCacheService).countCompletedProblems(courseId, userIds, lectureProblemSetIds);
        verify(learningEnrollmentPort, never()).countActiveStudentsByCourse(courseId);
        verify(learningEnrollmentPort, never()).findActiveStudentIdsByCourse(courseId, 1, 20);
        verify(learningLecturePort, never()).findLectureIdsByCourse(courseId);
        verify(learningCourseProblemPort, never()).findMainLectureProblemSetIdsByCourse(courseId);
        verify(lectureProgressRepository, never()).countCompletedByUserIdsAndLectureIds(userIds, lectureIds);
        verify(lectureProblemProgressRepository, never())
                .countCompletedByUserIdsAndLectureProblemSetIds(userIds, lectureProblemSetIds);
    }

    @Test
    void findStudentProgresses_withEmptyCourseItemIds_returnsZeroCountsWithoutBulkCountQueries() {
        Long courseId = 101L;
        Long userId = 10L;
        List<Long> userIds = List.of(userId);

        doReturn(1L).when(learningEnrollmentCacheService).countActiveStudentsByCourse(courseId);
        doReturn(userIds).when(learningEnrollmentCacheService).findActiveStudentIdsByCourse(courseId, 0, 20);
        doReturn(List.of()).when(learningCourseStructureCacheService).findLectureIdsByCourse(courseId);
        doReturn(List.of())
                .when(learningCourseStructureCacheService)
                .findMainLectureProblemSetIdsByCourse(courseId);
        given(learningUserPort.findUserNames(userIds)).willReturn(Map.of(userId, "student1"));

        var result = adminLearningProgressQueryService.findStudentProgresses(courseId, 0);

        assertEquals(1, result.content().size());
        assertEquals(0L, result.content().get(0).completedLectureCount());
        assertEquals(0L, result.content().get(0).totalLectureCount());
        assertEquals(0L, result.content().get(0).completedProblemCount());
        assertEquals(0L, result.content().get(0).totalProblemCount());
        verify(lectureProgressRepository, never())
                .countCompletedByUserIdsAndLectureIds(userIds, List.of());
        verify(lectureProblemProgressRepository, never())
                .countCompletedByUserIdsAndLectureProblemSetIds(userIds, List.of());
        verify(learningProgressCountCacheService, never()).countCompletedLectures(anyLong(), any(), any());
        verify(learningProgressCountCacheService, never()).countCompletedProblems(anyLong(), any(), any());
    }

    @Test
    void findStudentProgress_withEmptyCourseItemIds_returnsZeroCountsWithoutCountQueries() {
        Long courseId = 101L;
        Long userId = 10L;

        given(learningLecturePort.findLectureIdsByCourse(courseId)).willReturn(List.of());
        given(learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(courseId)).willReturn(List.of());
        given(learningUserPort.findUserName(userId)).willReturn("student1");

        StudentLearningProgress result = adminLearningProgressQueryService.findStudentProgress(courseId, userId);

        assertEquals(0L, result.completedLectureCount());
        assertEquals(0L, result.totalLectureCount());
        assertEquals(0L, result.completedProblemCount());
        assertEquals(0L, result.totalProblemCount());
        verify(lectureProgressRepository, never()).countCompletedByUserIdAndLectureIds(userId, List.of());
        verify(lectureProblemProgressRepository, never())
                .countCompletedByUserIdAndLectureProblemSetIds(userId, List.of());
    }

    @Test
    void findStudentProgresses_withNegativePage_usesFirstPage() {
        Long courseId = 101L;

        doReturn(21L).when(learningEnrollmentCacheService).countActiveStudentsByCourse(courseId);
        doReturn(List.of()).when(learningEnrollmentCacheService).findActiveStudentIdsByCourse(courseId, 0, 20);

        var result = adminLearningProgressQueryService.findStudentProgresses(courseId, -1);

        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(21L, result.totalElements());
        assertEquals(2, result.totalPages());
        assertTrue(result.hasNext());
        verify(learningEnrollmentCacheService).countActiveStudentsByCourse(courseId);
        verify(learningEnrollmentCacheService).findActiveStudentIdsByCourse(courseId, 0, 20);
        verify(learningEnrollmentPort, never()).countActiveStudentsByCourse(courseId);
        verify(learningEnrollmentPort, never()).findActiveStudentIdsByCourse(courseId, 0, 20);
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
        given(learningUserPort.findUserNames(List.of(userId))).willReturn(Map.of(userId, "student1"));
        given(lectureProgressRepository.countCompletedByUserIdsAndLectureIds(List.of(userId), lectureIds))
                .willReturn(Map.of(userId, 1L));
        given(lectureProblemProgressRepository.countCompletedByUserIdsAndLectureProblemSetIds(
                List.of(userId),
                lectureProblemSetIds
        )).willReturn(Map.of(userId, 2L));

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
        List<Long> userIds = List.of(userId);
        given(learningUserPort.findUserNames(userIds)).willReturn(Map.of(userId, "학생"));
        given(lectureProgressRepository.countCompletedByUserIdsAndLectureIds(userIds, lectureIds)).willReturn(Map.of(userId, 1L));
        given(lectureProblemProgressRepository.countCompletedByUserIdsAndLectureProblemSetIds(
                userIds,
                lectureProblemSetIds
        )).willReturn(Map.of(userId, 2L));

        LearningProgressSummary result = adminLearningProgressQueryService.summarizeLearningProgress();

        assertEquals(1L, result.totalCourseCount());
        assertEquals(1L, result.totalStudentCount());
        assertEquals(1L, result.completedLectureCount());
        assertEquals(2L, result.totalLectureCount());
        assertEquals(50, result.averageLectureProgressRate());
        assertEquals(2L, result.completedProblemCount());
    }
}
