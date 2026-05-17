package com.wanted.codebombalms.domain.course.service;

import com.wanted.codebombalms.domain.course.dto.request.CourseCreateRequest;
import com.wanted.codebombalms.domain.course.dto.request.CourseUpdateRequest;
import com.wanted.codebombalms.domain.course.dto.response.CourseDetailResponse;
import com.wanted.codebombalms.domain.course.dto.response.CourseResponse;
import com.wanted.codebombalms.domain.course.entity.Course;
import com.wanted.codebombalms.domain.course.enums.CourseStatus;
import com.wanted.codebombalms.domain.course.exception.CourseNotFoundException;
import com.wanted.codebombalms.domain.course.repository.CourseRepository;
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

    @InjectMocks
    private CourseService courseService;

    @Test
    @DisplayName("강좌 등록 시 CourseDetailResponse를 반환한다.")
    void 강좌_등록_테스트() {

        // given
        CourseCreateRequest request = new CourseCreateRequest(
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.ACTIVE
        );

        Course savedCourse = createCourse(
                1L,
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.ACTIVE
        );

        given(courseRepository.save(org.mockito.ArgumentMatchers.any(Course.class))).willReturn(savedCourse);

        // when
        CourseDetailResponse response = courseService.createCourse(request);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getCourseId());
        assertEquals(10L, response.getInstructorId());
        assertEquals("Java 기초 강좌", response.getTitle());
        assertEquals("Java 기초 문법을 학습하는 강좌입니다.", response.getDescription());
        assertEquals("java.png", response.getThumbnailUrl());
        assertEquals(CourseStatus.ACTIVE, response.getStatus());

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

        given(courseRepository.findByDeletedAtIsNull()).willReturn(List.of(course1, course2));

        // when
        List<CourseResponse> responses = courseService.findAllCourses();

        // then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getCourseId());
        assertEquals("Java 기초 강좌", responses.get(0).getTitle());
        assertEquals(2L, responses.get(1).getCourseId());
        assertEquals("Spring 기초 강좌", responses.get(1).getTitle());

        verify(courseRepository).findByDeletedAtIsNull();
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
        CourseDetailResponse response = courseService.findCourseById(courseId);

        // then
        assertNotNull(response);
        assertEquals(courseId, response.getCourseId());
        assertEquals(10L, response.getInstructorId());
        assertEquals("Java 기초 강좌", response.getTitle());
        assertEquals("Java 기초 문법을 학습하는 강좌입니다.", response.getDescription());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("존재하지 않는 강좌 상세 조회 시 CourseNotFoundException이 발생한다.")
    void 존재하지_않는_강좌_상세_조회_예외_테스트() {

        // given
        Long courseId = 999L;

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.empty());

        // when
        CourseNotFoundException exception = assertThrows(
                CourseNotFoundException.class,
                () -> courseService.findCourseById(courseId)
        );

        // then
        assertEquals("COURSE_NOT_FOUND", exception.getErrorCode());
        assertEquals(courseId, exception.getCourseId());

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

        CourseUpdateRequest request = new CourseUpdateRequest(
                "수정된 Java 강좌",
                "수정된 강좌 설명입니다.",
                "updated-java.png",
                CourseStatus.INACTIVE
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));

        // when
        CourseDetailResponse response = courseService.updateCourse(courseId, request);

        // then
        assertNotNull(response);
        assertEquals(courseId, response.getCourseId());
        assertEquals("수정된 Java 강좌", response.getTitle());
        assertEquals("수정된 강좌 설명입니다.", response.getDescription());
        assertEquals("updated-java.png", response.getThumbnailUrl());
        assertEquals(CourseStatus.INACTIVE, response.getStatus());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("존재하지 않는 강좌 수정 시 CourseNotFoundException이 발생한다.")
    void 존재하지_않는_강좌_수정_예외_테스트() {

        // given
        Long courseId = 999L;

        CourseUpdateRequest request = new CourseUpdateRequest(
                "수정된 Java 강좌",
                "수정된 강좌 설명입니다.",
                "updated-java.png",
                CourseStatus.INACTIVE
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.empty());

        // when
        CourseNotFoundException exception = assertThrows(
                CourseNotFoundException.class,
                () -> courseService.updateCourse(courseId, request)
        );

        // then
        assertEquals("COURSE_NOT_FOUND", exception.getErrorCode());
        assertEquals(courseId, exception.getCourseId());

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
        courseService.deleteCourse(courseId);

        // then
        assertEquals(CourseStatus.DELETED, course.getStatus());
        assertNotNull(course.getDeletedAt());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("존재하지 않는 강좌 삭제 시 CourseNotFoundException이 발생한다.")
    void 존재하지_않는_강좌_삭제_예외_테스트() {

        // given
        Long courseId = 999L;

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.empty());

        // when
        CourseNotFoundException exception = assertThrows(
                CourseNotFoundException.class,
                () -> courseService.deleteCourse(courseId)
        );

        // then
        assertEquals("COURSE_NOT_FOUND", exception.getErrorCode());
        assertEquals(courseId, exception.getCourseId());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
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