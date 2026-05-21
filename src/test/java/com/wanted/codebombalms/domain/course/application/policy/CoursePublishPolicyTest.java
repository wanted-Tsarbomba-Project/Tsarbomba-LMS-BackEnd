package com.wanted.codebombalms.domain.course.application.policy;

import com.wanted.codebombalms.course.application.policy.CoursePublishPolicy;
import com.wanted.codebombalms.course.application.port.LectureCatalogPort;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoursePublishPolicy 단위 테스트")
class CoursePublishPolicyTest {

    @Mock
    private LectureCatalogPort lectureCatalogPort;

    @InjectMocks
    private CoursePublishPolicy coursePublishPolicy;

    @Test
    @DisplayName("DRAFT 상태이고 강의가 있으면 개설 가능하다.")
    void 강좌_개설_가능_테스트() {

        // given
        Course course = createCourse(1L, CourseStatus.DRAFT);

        given(lectureCatalogPort.existsLectureInCourse(1L)).willReturn(true);

        // when & then
        coursePublishPolicy.validate(course);
    }

    @Test
    @DisplayName("DRAFT 상태가 아니면 개설할 수 없다.")
    void 강좌_개설_상태_예외_테스트() {

        // given
        Course course = createCourse(1L, CourseStatus.ACTIVE);

        // when
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> coursePublishPolicy.validate(course)
        );

        // then
        assertEquals(CourseErrorCode.COURSE_NOT_PUBLISHABLE_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("강의가 없으면 개설할 수 없다.")
    void 강좌_개설_강의_예외_테스트() {

        // given
        Course course = createCourse(1L, CourseStatus.DRAFT);

        given(lectureCatalogPort.existsLectureInCourse(1L)).willReturn(false);

        // when
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> coursePublishPolicy.validate(course)
        );

        // then
        assertEquals(CourseErrorCode.COURSE_LECTURE_REQUIRED, exception.getErrorCode());
    }

    private Course createCourse(Long courseId, CourseStatus status) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setInstructorId(10L);
        course.setTitle("Java 기초 강좌");
        course.setDescription("Java 기초 문법을 학습하는 강좌입니다.");
        course.setThumbnailUrl("java.png");
        course.setStatus(status);
        return course;
    }
}
