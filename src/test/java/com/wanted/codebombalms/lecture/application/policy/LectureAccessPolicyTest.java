package com.wanted.codebombalms.lecture.application.policy;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
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
    void validateLearningContentAccess_throwsForbidden_whenUserIdIsNull() {
        Lecture lecture = lecture(1L);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> lectureAccessPolicy.validateLearningContentAccess(lecture, null, false)
        );

        assertEquals(LectureErrorCode.LECTURE_ACCESS_DENIED, exception.getErrorCode());
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
    void validatePreviousLecturesCompleted_allowsFirstLecture() {
        lectureAccessPolicy.validatePreviousLecturesCompleted(10L, List.of());

        verify(lectureProgressPort, never()).areLecturesCompleted(10L, List.of());
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
        Course course = new Course();
        course.setCourseId(courseId);
        Lecture lecture = new Lecture();
        lecture.setCourse(course);
        return lecture;
    }
}
