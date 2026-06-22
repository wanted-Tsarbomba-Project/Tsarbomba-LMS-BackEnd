package com.wanted.codebombalms.lecture.application.policy;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.lecture.application.port.LectureEnrollmentPort;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
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

    private Lecture lecture(Long courseId) {
        Course course = new Course();
        course.setCourseId(courseId);
        Lecture lecture = new Lecture();
        lecture.setCourse(course);
        return lecture;
    }
}
