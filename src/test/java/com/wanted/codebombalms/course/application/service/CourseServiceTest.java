package com.wanted.codebombalms.course.application.service;

import com.wanted.codebombalms.course.application.command.CreateCourseCommand;
import com.wanted.codebombalms.course.application.command.PublishCourseCommand;
import com.wanted.codebombalms.course.application.command.UpdateCourseCommand;
import com.wanted.codebombalms.course.application.policy.CourseAuthorPolicy;
import com.wanted.codebombalms.course.application.policy.CourseCategoryPolicy;
import com.wanted.codebombalms.course.application.policy.CoursePublishPolicy;
import com.wanted.codebombalms.course.application.port.LectureManagementPort;
import com.wanted.codebombalms.course.application.service.CourseCommandService;
import com.wanted.codebombalms.course.application.service.CourseQueryService;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
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
    private CourseAuthorPolicy courseAuthorPolicy;

    @Mock
    private CourseCategoryPolicy courseCategoryPolicy;

    @Mock
    private CoursePublishPolicy coursePublishPolicy;

    @Mock
    private LectureManagementPort lectureManagementPort;

    @InjectMocks
    private CourseCommandService courseCommandService;

    @InjectMocks
    private CourseQueryService courseQueryService;

    @Test
    void createCourse_returnsCourse() {
        CreateCourseCommand command = new CreateCourseCommand(10L, 1L, "Java", "description", "java.png");
        Course savedCourse = createCourse(1L, 10L, "Java", "description", "java.png", CourseStatus.DRAFT);

        given(courseRepository.save(any(Course.class))).willReturn(savedCourse);

        Course result = courseCommandService.createCourse(command);

        assertEquals(1L, result.getCourseId());
        assertEquals(10L, result.getInstructorId());
        assertEquals("Java", result.getTitle());
        assertEquals(CourseStatus.DRAFT, result.getStatus());
        verify(courseAuthorPolicy).validateOperator(command.instructorId());
        verify(courseCategoryPolicy).validateActiveCategory(command.courseCategoryId());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void findAllCourses_returnsActiveCoursesOnly() {
        Course course1 = createCourse(1L, 10L, "Java", "description", "java.png", CourseStatus.ACTIVE);
        Course course2 = createCourse(2L, 10L, "Spring", "description", "spring.png", CourseStatus.ACTIVE);

        given(courseRepository.findByStatusAndDeletedAtIsNull(CourseStatus.ACTIVE)).willReturn(List.of(course1, course2));

        List<Course> results = courseQueryService.findAllCourses(null);

        assertEquals(2, results.size());
        assertEquals("Java", results.get(0).getTitle());
        assertEquals("Spring", results.get(1).getTitle());
        verify(courseRepository).findByStatusAndDeletedAtIsNull(CourseStatus.ACTIVE);
    }

    @Test
    void findAllCourses_returnsActiveCoursesByCategory() {
        Long courseCategoryId = 1L;
        Course course = createCourse(1L, 10L, "Java", "description", "java.png", CourseStatus.ACTIVE);

        given(courseRepository.findByCourseCategoryIdAndStatusAndDeletedAtIsNull(courseCategoryId, CourseStatus.ACTIVE))
                .willReturn(List.of(course));

        List<Course> results = courseQueryService.findAllCourses(courseCategoryId);

        assertEquals(1, results.size());
        assertEquals("Java", results.get(0).getTitle());
        verify(courseRepository).findByCourseCategoryIdAndStatusAndDeletedAtIsNull(courseCategoryId, CourseStatus.ACTIVE);
    }

    @Test
    void findCoursesByInstructor_returnsInstructorCourses() {
        Long instructorId = 10L;
        Course activeCourse = createCourse(1L, instructorId, "Java", "description", "java.png", CourseStatus.ACTIVE);
        Course draftCourse = createCourse(2L, instructorId, "Spring", "description", "spring.png", CourseStatus.DRAFT);

        given(courseRepository.findByInstructorIdAndDeletedAtIsNull(instructorId))
                .willReturn(List.of(activeCourse, draftCourse));

        List<Course> results = courseQueryService.findCoursesByInstructor(instructorId);

        assertEquals(2, results.size());
        assertEquals("Java", results.get(0).getTitle());
        assertEquals("Spring", results.get(1).getTitle());
        verify(courseRepository).findByInstructorIdAndDeletedAtIsNull(instructorId);
    }

    @Test
    void findCourseById_returnsActiveCourse() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java", "description", "java.png", CourseStatus.ACTIVE);

        given(courseRepository.findByCourseIdAndStatusAndDeletedAtIsNull(courseId, CourseStatus.ACTIVE))
                .willReturn(Optional.of(course));

        Course result = courseQueryService.findCourseById(courseId);

        assertEquals(courseId, result.getCourseId());
        assertEquals("Java", result.getTitle());
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
                1L,
                "Updated Java",
                "updated",
                "updated-java.png",
                CourseStatus.INACTIVE
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));
        given(courseRepository.save(course)).willReturn(course);

        Course result = courseCommandService.updateCourse(command);

        assertEquals("Updated Java", result.getTitle());
        assertEquals("updated", result.getDescription());
        assertEquals(CourseStatus.INACTIVE, result.getStatus());
        verify(courseCategoryPolicy).validateActiveCategory(command.courseCategoryId());
        verify(courseRepository).save(course);
    }

    @Test
    void updateCourse_throwsValidation_whenActivatingDraftDirectly() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java", "description", "java.png", CourseStatus.DRAFT);
        UpdateCourseCommand command = new UpdateCourseCommand(courseId, null, null, null, null, CourseStatus.ACTIVE);

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
        UpdateCourseCommand command = new UpdateCourseCommand(courseId, null, null, null, null, CourseStatus.DELETED);

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
        verify(lectureManagementPort).deleteLecturesByCourseId(courseId);
    }

    @Test
    void publishCourse_activatesDraftCourse() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java", "description", "java.png", CourseStatus.DRAFT);

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));
        given(courseRepository.save(course)).willReturn(course);

        Course result = courseCommandService.publishCourse(new PublishCourseCommand(courseId));

        assertEquals(CourseStatus.ACTIVE, result.getStatus());
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
