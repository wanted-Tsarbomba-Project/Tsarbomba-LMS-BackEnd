package com.wanted.codebombalms.lecture.application.policy;

import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.lecture.application.port.LectureEnrollmentPort;
import com.wanted.codebombalms.lecture.application.port.LectureProgressPort;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LectureAccessPolicyTest {

    @Mock
    private LectureEnrollmentPort lectureEnrollmentPort;

    @Mock
    private LectureProgressPort lectureProgressPort;

    @InjectMocks
    private LectureAccessPolicy lectureAccessPolicy;

    @Test
    void validateLearningContentAccess_allowsOperatorWithoutEnrollmentCheck() {
        Lecture lecture = lecture(1L);

        lectureAccessPolicy.validateLearningContentAccess(lecture, null, true);

        verify(lectureEnrollmentPort, never()).isActiveStudentOfCourse(1L, null);
    }

    @Test
    void validateLearningContentAccess_allowsStudent_whenStudentIsEnrolled() {
        Long userId = 10L;
        Lecture lecture = lecture(1L);
        given(lectureEnrollmentPort.isActiveStudentOfCourse(1L, userId)).willReturn(true);

        assertDoesNotThrow(
                () -> lectureAccessPolicy.validateLearningContentAccess(lecture, userId, false)
        );

        verify(lectureEnrollmentPort).isActiveStudentOfCourse(1L, userId);
    }

    @Test
    void validateLearningContentAccess_throwsUnauthorized_whenUserIdIsNull() {
        Lecture lecture = lecture(1L);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> lectureAccessPolicy.validateLearningContentAccess(lecture, null, false)
        );

        assertEquals(AuthErrorCode.AUTH_REQUIRED, exception.getErrorCode());
        verify(lectureEnrollmentPort, never()).isActiveStudentOfCourse(1L, null);
    }

    @Test
    void validateLearningContentAccess_throwsForbidden_whenStudentIsNotEnrolled() {
        Long userId = 10L;
        Lecture lecture = lecture(1L);
        given(lectureEnrollmentPort.isActiveStudentOfCourse(1L, userId)).willReturn(false);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> lectureAccessPolicy.validateLearningContentAccess(lecture, userId, false)
        );

        assertEquals(LectureErrorCode.LECTURE_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    void validateLearningContentAccess_throwsNotFoundForDeletedCourseWithoutEnrollmentCheck() {
        Lecture lecture = new Lecture();
        lecture.setCourse(course(1L, CourseStatus.DELETED));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> lectureAccessPolicy.validateLearningContentAccess(lecture, 10L, false)
        );

        assertEquals(CourseErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(lectureEnrollmentPort);
    }

    @Test
    void validateCourseContentAccess_allowsActiveCourseWithoutEnrollmentCheck() {
        Course course = course(1L, CourseStatus.ACTIVE);

        assertDoesNotThrow(
                () -> lectureAccessPolicy.validateCourseContentAccess(course, null, false)
        );

        verifyNoInteractions(lectureEnrollmentPort);
    }

    @Test
    void validateCourseContentAccess_allowsInactiveCourse_whenStudentIsEnrolled() {
        Long userId = 10L;
        Course course = course(1L, CourseStatus.INACTIVE);
        given(lectureEnrollmentPort.isActiveStudentOfCourse(1L, userId)).willReturn(true);

        assertDoesNotThrow(
                () -> lectureAccessPolicy.validateCourseContentAccess(course, userId, false)
        );
    }

    @Test
    void validateCourseContentAccess_throwsUnauthorizedForInactiveCourse_whenUserIdIsNull() {
        Course course = course(1L, CourseStatus.INACTIVE);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> lectureAccessPolicy.validateCourseContentAccess(course, null, false)
        );

        assertEquals(AuthErrorCode.AUTH_REQUIRED, exception.getErrorCode());
        verify(lectureEnrollmentPort, never()).isActiveStudentOfCourse(1L, null);
    }

    @Test
    void validateCourseContentAccess_throwsForbiddenForInactiveCourse_whenStudentIsNotEnrolled() {
        Long userId = 10L;
        Course course = course(1L, CourseStatus.INACTIVE);
        given(lectureEnrollmentPort.isActiveStudentOfCourse(1L, userId)).willReturn(false);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> lectureAccessPolicy.validateCourseContentAccess(course, userId, false)
        );

        assertEquals(LectureErrorCode.LECTURE_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    void validateCourseContentAccess_throwsForbiddenForDraftCourseWithoutEnrollmentCheck() {
        Course course = course(1L, CourseStatus.DRAFT);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> lectureAccessPolicy.validateCourseContentAccess(course, 10L, false)
        );

        assertEquals(LectureErrorCode.LECTURE_ACCESS_DENIED, exception.getErrorCode());
        verifyNoInteractions(lectureEnrollmentPort);
    }

    @Test
    void validateCourseContentAccess_throwsNotFoundForDeletedCourseWithoutEnrollmentCheck() {
        Course course = course(1L, CourseStatus.DELETED);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> lectureAccessPolicy.validateCourseContentAccess(course, 10L, false)
        );

        assertEquals(CourseErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(lectureEnrollmentPort);
    }

    @Test
    void validateCourseContentAccess_allowsOperatorForDraftCourse() {
        Course course = course(1L, CourseStatus.DRAFT);

        assertDoesNotThrow(
                () -> lectureAccessPolicy.validateCourseContentAccess(course, null, true)
        );

        verifyNoInteractions(lectureEnrollmentPort);
    }

    @Test
    void validatePreviousLecturesCompleted_allowsFirstLecture() {
        lectureAccessPolicy.validatePreviousLecturesCompleted(10L, List.of());

        verifyNoInteractions(lectureProgressPort);
    }

    @Test
    void validatePreviousLecturesCompleted_allowsWhenAllPreviousLecturesCompleted() {
        Long userId = 10L;
        List<Long> previousLectureIds = List.of(1L, 2L);
        given(lectureProgressPort.areLecturesCompleted(userId, previousLectureIds)).willReturn(true);

        assertDoesNotThrow(
                () -> lectureAccessPolicy.validatePreviousLecturesCompleted(userId, previousLectureIds)
        );
    }

    @Test
    void validatePreviousLecturesCompleted_throwsForbiddenWhenPreviousLectureNotCompleted() {
        Long userId = 10L;
        List<Long> previousLectureIds = List.of(1L, 2L);
        given(lectureProgressPort.areLecturesCompleted(userId, previousLectureIds)).willReturn(false);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> lectureAccessPolicy.validatePreviousLecturesCompleted(userId, previousLectureIds)
        );

        assertEquals(LectureErrorCode.PREVIOUS_LECTURE_NOT_COMPLETED, exception.getErrorCode());
    }

    private Lecture lecture(Long courseId) {
        Course course = course(courseId, CourseStatus.ACTIVE);
        Lecture lecture = new Lecture();
        lecture.setCourse(course);
        return lecture;
    }

    private Course course(Long courseId, CourseStatus status) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setStatus(status);
        return course;
    }
}
