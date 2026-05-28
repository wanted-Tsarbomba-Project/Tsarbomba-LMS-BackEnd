package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
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
