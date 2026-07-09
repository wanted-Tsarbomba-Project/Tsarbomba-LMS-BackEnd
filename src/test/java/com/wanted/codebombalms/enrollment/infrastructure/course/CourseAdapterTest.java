package com.wanted.codebombalms.enrollment.infrastructure.course;

import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CourseAdapterTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseAdapter courseAdapter;

    @Test
    void getPublicationStatus_throwsNotFound_whenCourseStatusIsDeleted() {
        Long courseId = 1L;
        Course course = new Course();
        course.setCourseId(courseId);
        course.setStatus(CourseStatus.DELETED);

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId))
                .willReturn(Optional.of(course));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> courseAdapter.getPublicationStatus(courseId)
        );

        assertEquals(CourseErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }
}
