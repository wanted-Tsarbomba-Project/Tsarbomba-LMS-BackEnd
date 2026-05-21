package com.wanted.codebombalms.domain.course.application.service;

import com.wanted.codebombalms.domain.course.application.command.CreateCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.PublishCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.UpdateCourseCommand;
import com.wanted.codebombalms.domain.course.application.policy.CoursePublishPolicy;
import com.wanted.codebombalms.domain.course.application.result.CourseDetailResult;
import com.wanted.codebombalms.domain.course.application.result.CourseSummaryResult;
import com.wanted.codebombalms.domain.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.course.domain.model.CourseStatus;
import com.wanted.codebombalms.domain.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService unit test")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CoursePublishPolicy coursePublishPolicy;

    @InjectMocks
    private CourseCommandService courseCommandService;

    @InjectMocks
    private CourseQueryService courseQueryService;

    @Test
    void createCourse_returnsDetailResult() {
        CreateCourseCommand command = new CreateCourseCommand(10L, "Java", "description", "java.png");
        Course savedCourse = createCourse(1L, 10L, "Java", "description", "java.png", CourseStatus.DRAFT);

        given(courseRepository.save(any(Course.class))).willReturn(savedCourse);

        CourseDetailResult result = courseCommandService.createCourse(command);

        assertEquals(1L, result.courseId());
        assertEquals(10L, result.instructorId());
        assertEquals("Java", result.title());
        assertEquals(CourseStatus.DRAFT, result.status());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void findAllCourses_returnsActiveCoursesOnly() {
        Course course1 = createCourse(1L, 10L, "Java", "description", "java.png", CourseStatus.ACTIVE);
        Course course2 = createCourse(2L, 10L, "Spring", "description", "spring.png", CourseStatus.ACTIVE);

        given(courseRepository.findByStatusAndDeletedAtIsNull(CourseStatus.ACTIVE)).willReturn(List.of(course1, course2));

        List<CourseSummaryResult> results = courseQueryService.findAllCourses();

        assertEquals(2, results.size());
        assertEquals("Java", results.get(0).title());
        assertEquals("Spring", results.get(1).title());
        verify(courseRepository).findByStatusAndDeletedAtIsNull(CourseStatus.ACTIVE);
    }

    @Test
    void findCourseById_returnsActiveCourse() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java", "description", "java.png", CourseStatus.ACTIVE);

        given(courseRepository.findByCourseIdAndStatusAndDeletedAtIsNull(courseId, CourseStatus.ACTIVE))
                .willReturn(Optional.of(course));

        CourseDetailResult result = courseQueryService.findCourseById(courseId);

        assertEquals(courseId, result.courseId());
        assertEquals("Java", result.title());
        verify(courseRepository).findByCourseIdAndStatusAndDeletedAtIsNull(courseId, CourseStatus.ACTIVE);
    }

    @Test
    void findCourseById_throwsNotFound_whenCourseIsNotActive() {
        Long courseId = 1L;

        given(courseRepository.findByCourseIdAndStatusAndDeletedAtIsNull(courseId, CourseStatus.ACTIVE))
                .willReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> courseQueryService.findCourseById(courseId)
        );

        assertEquals(CourseErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateCourse_updatesEditableFields() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java", "description", "java.png", CourseStatus.ACTIVE);
        UpdateCourseCommand command = new UpdateCourseCommand(
                courseId,
                "Updated Java",
                "updated",
                "updated-java.png",
                CourseStatus.INACTIVE
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));
        given(courseRepository.save(course)).willReturn(course);

        CourseDetailResult result = courseCommandService.updateCourse(command);

        assertEquals("Updated Java", result.title());
        assertEquals("updated", result.description());
        assertEquals(CourseStatus.INACTIVE, result.status());
        verify(courseRepository).save(course);
    }

    @Test
    void updateCourse_throwsValidation_whenActivatingDraftDirectly() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java", "description", "java.png", CourseStatus.DRAFT);
        UpdateCourseCommand command = new UpdateCourseCommand(courseId, null, null, null, CourseStatus.ACTIVE);

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> courseCommandService.updateCourse(command)
        );

        assertEquals(CourseErrorCode.COURSE_ACTIVE_STATUS_REQUIRES_PUBLISH, exception.getErrorCode());
    }

    @Test
    void updateCourse_throwsValidation_whenDeletingByStatus() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java", "description", "java.png", CourseStatus.ACTIVE);
        UpdateCourseCommand command = new UpdateCourseCommand(courseId, null, null, null, CourseStatus.DELETED);

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> courseCommandService.updateCourse(command)
        );

        assertEquals(CourseErrorCode.COURSE_DELETE_STATUS_REQUIRES_DELETE, exception.getErrorCode());
    }

    @Test
    void deleteCourse_marksDeleted() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java", "description", "java.png", CourseStatus.ACTIVE);

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));

        courseCommandService.deleteCourse(courseId);

        assertEquals(CourseStatus.DELETED, course.getStatus());
        assertNotNull(course.getDeletedAt());
        verify(courseRepository).save(course);
    }

    @Test
    void publishCourse_activatesDraftCourse() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java", "description", "java.png", CourseStatus.DRAFT);

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));
        given(courseRepository.save(course)).willReturn(course);

        CourseDetailResult result = courseCommandService.publishCourse(new PublishCourseCommand(courseId));

        assertEquals(CourseStatus.ACTIVE, result.status());
        verify(coursePublishPolicy).validate(course);
        verify(courseRepository).save(course);
    }

    private Course createCourse(
            Long courseId,
            Long instructorId,
            String title,
            String description,
            String thumbnailUrl,
            CourseStatus status
    ) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setInstructorId(instructorId);
        course.setTitle(title);
        course.setDescription(description);
        course.setThumbnailUrl(thumbnailUrl);
        course.setStatus(status);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        return course;
    }
}
