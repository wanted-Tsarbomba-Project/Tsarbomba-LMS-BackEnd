package com.wanted.codebombalms.domain.course.application.service;

import com.wanted.codebombalms.domain.course.application.command.CreateCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.PublishCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.UpdateCourseCommand;
import com.wanted.codebombalms.domain.course.application.policy.CoursePublishPolicy;
import com.wanted.codebombalms.domain.course.presentation.api.response.CourseDetailResponse;
import com.wanted.codebombalms.domain.course.presentation.api.response.CourseResponse;
import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.course.domain.model.CourseStatus;
import com.wanted.codebombalms.domain.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService 단위 테스트")
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
    @DisplayName("강좌 등록 시 CourseDetailResponse를 반환한다.")
    void 강좌_등록_테스트() {

        // given
        CreateCourseCommand command = new CreateCourseCommand(
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png"
        );

        Course savedCourse = createCourse(
                1L,
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.DRAFT
        );

        given(courseRepository.save(org.mockito.ArgumentMatchers.any(Course.class))).willReturn(savedCourse);

        // when
        CourseDetailResponse response = courseCommandService.createCourse(command);

        // then
        assertNotNull(response);
        assertEquals(1L, response.courseId());
        assertEquals(10L, response.instructorId());
        assertEquals("Java 기초 강좌", response.title());
        assertEquals("Java 기초 문법을 학습하는 강좌입니다.", response.description());
        assertEquals("java.png", response.thumbnailUrl());
        assertEquals(CourseStatus.DRAFT, response.status());

        verify(courseRepository).save(org.mockito.ArgumentMatchers.any(Course.class));
    }

    @Test
    @DisplayName("삭제되지 않은 강좌 목록을 조회한다.")
    void 강좌_목록_조회_테스트() {

        // given
        Course course1 = createCourse(
                1L,
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.ACTIVE
        );

        Course course2 = createCourse(
                2L,
                10L,
                "Spring 기초 강좌",
                "Spring 기초를 학습하는 강좌입니다.",
                "spring.png",
                CourseStatus.ACTIVE
        );

        given(courseRepository.findByStatusAndDeletedAtIsNull(CourseStatus.ACTIVE)).willReturn(List.of(course1, course2));

        // when
        List<CourseResponse> responses = courseQueryService.findAllCourses();

        // then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).courseId());
        assertEquals("Java 기초 강좌", responses.get(0).title());
        assertEquals(2L, responses.get(1).courseId());
        assertEquals("Spring 기초 강좌", responses.get(1).title());

        verify(courseRepository).findByStatusAndDeletedAtIsNull(CourseStatus.ACTIVE);
    }

    @Test
    @DisplayName("courseId로 강좌 상세 정보를 조회한다.")
    void 강좌_상세_조회_테스트() {

        // given
        Long courseId = 1L;

        Course course = createCourse(
                courseId,
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.ACTIVE
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));

        // when
        CourseDetailResponse response = courseQueryService.findCourseById(courseId);

        // then
        assertNotNull(response);
        assertEquals(courseId, response.courseId());
        assertEquals(10L, response.instructorId());
        assertEquals("Java 기초 강좌", response.title());
        assertEquals("Java 기초 문법을 학습하는 강좌입니다.", response.description());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("존재하지 않는 강좌 상세 조회 시 NotFoundException이 발생한다.")
    void 존재하지_않는_강좌_상세_조회_예외_테스트() {

        // given
        Long courseId = 999L;

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> courseQueryService.findCourseById(courseId)
        );

        // then
        assertNotNull(exception);

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("강좌 정보를 수정한다.")
    void 강좌_수정_테스트() {

        // given
        Long courseId = 1L;

        Course course = createCourse(
                courseId,
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.ACTIVE
        );

        UpdateCourseCommand command = new UpdateCourseCommand(
                courseId,
                "수정된 Java 강좌",
                "수정된 강좌 설명입니다.",
                "updated-java.png",
                CourseStatus.INACTIVE
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));
        given(courseRepository.save(course)).willReturn(course);

        // when
        CourseDetailResponse response = courseCommandService.updateCourse(command);

        // then
        assertNotNull(response);
        assertEquals(courseId, response.courseId());
        assertEquals("수정된 Java 강좌", response.title());
        assertEquals("수정된 강좌 설명입니다.", response.description());
        assertEquals("updated-java.png", response.thumbnailUrl());
        assertEquals(CourseStatus.INACTIVE, response.status());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("존재하지 않는 강좌 수정 시 NotFoundException이 발생한다.")
    void 존재하지_않는_강좌_수정_예외_테스트() {

        // given
        Long courseId = 999L;

        UpdateCourseCommand command = new UpdateCourseCommand(
                courseId,
                "수정된 Java 강좌",
                "수정된 강좌 설명입니다.",
                "updated-java.png",
                CourseStatus.INACTIVE
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> courseCommandService.updateCourse(command)
        );

        // then
        assertNotNull(exception);

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("강좌 삭제 시 상태를 DELETED로 변경하고 deletedAt을 기록한다.")
    void 강좌_삭제_테스트() {

        // given
        Long courseId = 1L;

        Course course = createCourse(
                courseId,
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.ACTIVE
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));

        // when
        courseCommandService.deleteCourse(courseId);

        // then
        assertEquals(CourseStatus.DELETED, course.getStatus());
        assertNotNull(course.getDeletedAt());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("존재하지 않는 강좌 삭제 시 NotFoundException이 발생한다.")
    void 존재하지_않는_강좌_삭제_예외_테스트() {

        // given
        Long courseId = 999L;

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> courseCommandService.deleteCourse(courseId)
        );

        // then
        assertNotNull(exception);

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("강좌 개설 시 정책 검증 후 상태를 ACTIVE로 변경한다.")
    void 강좌_개설_테스트() {

        // given
        Long courseId = 1L;

        Course course = createCourse(
                courseId,
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.DRAFT
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));
        given(courseRepository.save(course)).willReturn(course);

        // when
        CourseDetailResponse response = courseCommandService.publishCourse(new PublishCourseCommand(courseId));

        // then
        assertNotNull(response);
        assertEquals(courseId, response.courseId());
        assertEquals(CourseStatus.ACTIVE, response.status());

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
