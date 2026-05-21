package com.wanted.codebombalms.domain.lecture.application.service;

import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.domain.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureCreateRequest;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureUpdateRequest;
import com.wanted.codebombalms.domain.lecture.presentation.api.response.LectureDetailResponse;
import com.wanted.codebombalms.domain.lecture.presentation.api.response.LectureResponse;
import com.wanted.codebombalms.domain.lecture.domain.model.Lecture;
import com.wanted.codebombalms.domain.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.domain.lecture.domain.exception.LectureNotFoundException;
import com.wanted.codebombalms.domain.lecture.domain.repository.LectureRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("LectureService 단위 테스트")
class LectureServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private LectureService lectureService;

    @Test
    @DisplayName("강의 등록 시 LectureDetailResponse를 반환한다.")
    void 강의_등록_테스트() {

        // given
        Long courseId = 1L;

        Course course = createCourse(courseId, 10L, "Java 기초 강좌");

        LectureCreateRequest request = new LectureCreateRequest(
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                1,
                LectureStatus.ACTIVE
        );

        Lecture savedLecture = createLecture(
                1L,
                course,
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                LectureStatus.ACTIVE,
                1
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId))
                .willReturn(Optional.of(course));
        given(lectureRepository.save(any(Lecture.class)))
                .willReturn(savedLecture);

        // when
        LectureDetailResponse response = lectureService.createLecture(courseId, request);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getLectureId());
        assertEquals(courseId, response.getCourseId());
        assertEquals(10L, response.getInstructorId());
        assertEquals("Java 1강", response.getTitle());
        assertEquals("Java 기본 문법을 학습하는 강의입니다.", response.getDescription());
        assertEquals("java-1.mp4", response.getVideoUrl());
        assertEquals("java-1.png", response.getThumbnailUrl());
        assertEquals(LectureStatus.ACTIVE, response.getStatus());
        assertEquals(1, response.getLectureOrder());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
        verify(lectureRepository).save(any(Lecture.class));
    }

    @Test
    @DisplayName("존재하지 않는 강좌에 강의 등록 시 CourseNotFoundException이 발생한다.")
    void 존재하지_않는_강좌에_강의_등록_예외_테스트() {

        // given
        Long courseId = 999L;

        LectureCreateRequest request = new LectureCreateRequest(
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                1,
                LectureStatus.ACTIVE
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId))
                .willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> lectureService.createLecture(courseId, request)
        );

        // then
        assertEquals(CourseErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("특정 강좌의 강의 목록을 lectureOrder 오름차순으로 조회한다.")
    void 강의_목록_조회_테스트() {

        // given
        Long courseId = 1L;

        Course course = createCourse(courseId, 10L, "Java 기초 강좌");

        Lecture lecture1 = createLecture(
                1L,
                course,
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                LectureStatus.ACTIVE,
                1
        );

        Lecture lecture2 = createLecture(
                2L,
                course,
                "Java 2강",
                "Java 객체지향을 학습하는 강의입니다.",
                "java-2.mp4",
                "java-2.png",
                LectureStatus.ACTIVE,
                2
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId))
                .willReturn(Optional.of(course));
        given(lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId))
                .willReturn(List.of(lecture1, lecture2));

        // when
        List<LectureResponse> responses = lectureService.findLecturesByCourseId(courseId);

        // then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getLectureId());
        assertEquals("Java 1강", responses.get(0).getTitle());
        assertEquals(1, responses.get(0).getLectureOrder());
        assertEquals(2L, responses.get(1).getLectureId());
        assertEquals("Java 2강", responses.get(1).getTitle());
        assertEquals(2, responses.get(1).getLectureOrder());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
        verify(lectureRepository).findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId);
    }

    @Test
    @DisplayName("존재하지 않는 강좌의 강의 목록 조회 시 CourseNotFoundException이 발생한다.")
    void 존재하지_않는_강좌의_강의_목록_조회_예외_테스트() {

        // given
        Long courseId = 999L;

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId))
                .willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> lectureService.findLecturesByCourseId(courseId)
        );

        // then
        assertEquals(CourseErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("lectureId로 강의 상세 정보를 조회한다.")
    void 강의_상세_조회_테스트() {

        // given
        Long lectureId = 1L;

        Course course = createCourse(1L, 10L, "Java 기초 강좌");

        Lecture lecture = createLecture(
                lectureId,
                course,
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                LectureStatus.ACTIVE,
                1
        );

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId))
                .willReturn(Optional.of(lecture));

        // when
        LectureDetailResponse response = lectureService.findLectureById(lectureId);

        // then
        assertNotNull(response);
        assertEquals(lectureId, response.getLectureId());
        assertEquals(1L, response.getCourseId());
        assertEquals(10L, response.getInstructorId());
        assertEquals("Java 1강", response.getTitle());
        assertEquals("Java 기본 문법을 학습하는 강의입니다.", response.getDescription());
        assertEquals("java-1.mp4", response.getVideoUrl());
        assertEquals(1, response.getLectureOrder());

        verify(lectureRepository).findByLectureIdAndDeletedAtIsNull(lectureId);
    }

    @Test
    @DisplayName("존재하지 않는 강의 상세 조회 시 LectureNotFoundException이 발생한다.")
    void 존재하지_않는_강의_상세_조회_예외_테스트() {

        // given
        Long lectureId = 999L;

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId))
                .willReturn(Optional.empty());

        // when
        LectureNotFoundException exception = assertThrows(
                LectureNotFoundException.class,
                () -> lectureService.findLectureById(lectureId)
        );

        // then
        assertEquals("LECTURE_NOT_FOUND", exception.getErrorCode());
        assertEquals(lectureId, exception.getLectureId());

        verify(lectureRepository).findByLectureIdAndDeletedAtIsNull(lectureId);
    }

    @Test
    @DisplayName("강의 정보를 수정한다.")
    void 강의_수정_테스트() {

        // given
        Long lectureId = 1L;

        Course course = createCourse(1L, 10L, "Java 기초 강좌");

        Lecture lecture = createLecture(
                lectureId,
                course,
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                LectureStatus.ACTIVE,
                1
        );

        LectureUpdateRequest request = new LectureUpdateRequest(
                "수정된 Java 1강",
                "수정된 강의 설명입니다.",
                "updated-java-1.mp4",
                "updated-java-1.png",
                2,
                LectureStatus.INACTIVE
        );

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId))
                .willReturn(Optional.of(lecture));

        // when
        LectureDetailResponse response = lectureService.updateLecture(lectureId, request);

        // then
        assertNotNull(response);
        assertEquals(lectureId, response.getLectureId());
        assertEquals("수정된 Java 1강", response.getTitle());
        assertEquals("수정된 강의 설명입니다.", response.getDescription());
        assertEquals("updated-java-1.mp4", response.getVideoUrl());
        assertEquals("updated-java-1.png", response.getThumbnailUrl());
        assertEquals(2, response.getLectureOrder());
        assertEquals(LectureStatus.INACTIVE, response.getStatus());

        verify(lectureRepository).findByLectureIdAndDeletedAtIsNull(lectureId);
    }

    @Test
    @DisplayName("존재하지 않는 강의 수정 시 LectureNotFoundException이 발생한다.")
    void 존재하지_않는_강의_수정_예외_테스트() {

        // given
        Long lectureId = 999L;

        LectureUpdateRequest request = new LectureUpdateRequest(
                "수정된 Java 1강",
                "수정된 강의 설명입니다.",
                "updated-java-1.mp4",
                "updated-java-1.png",
                2,
                LectureStatus.INACTIVE
        );

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId))
                .willReturn(Optional.empty());

        // when
        LectureNotFoundException exception = assertThrows(
                LectureNotFoundException.class,
                () -> lectureService.updateLecture(lectureId, request)
        );

        // then
        assertEquals("LECTURE_NOT_FOUND", exception.getErrorCode());
        assertEquals(lectureId, exception.getLectureId());

        verify(lectureRepository).findByLectureIdAndDeletedAtIsNull(lectureId);
    }

    @Test
    @DisplayName("강의 삭제 시 상태를 DELETED로 변경하고 deletedAt을 기록한다.")
    void 강의_삭제_테스트() {

        // given
        Long lectureId = 1L;

        Course course = createCourse(1L, 10L, "Java 기초 강좌");

        Lecture lecture = createLecture(
                lectureId,
                course,
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                LectureStatus.ACTIVE,
                1
        );

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId))
                .willReturn(Optional.of(lecture));

        // when
        lectureService.deleteLecture(lectureId);

        // then
        assertEquals(LectureStatus.DELETED, lecture.getStatus());
        assertNotNull(lecture.getDeletedAt());

        verify(lectureRepository).findByLectureIdAndDeletedAtIsNull(lectureId);
    }

    @Test
    @DisplayName("존재하지 않는 강의 삭제 시 LectureNotFoundException이 발생한다.")
    void 존재하지_않는_강의_삭제_예외_테스트() {

        // given
        Long lectureId = 999L;

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId))
                .willReturn(Optional.empty());

        // when
        LectureNotFoundException exception = assertThrows(
                LectureNotFoundException.class,
                () -> lectureService.deleteLecture(lectureId)
        );

        // then
        assertEquals("LECTURE_NOT_FOUND", exception.getErrorCode());
        assertEquals(lectureId, exception.getLectureId());

        verify(lectureRepository).findByLectureIdAndDeletedAtIsNull(lectureId);
    }

    private Course createCourse(Long courseId, Long instructorId, String title) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setInstructorId(instructorId);
        course.setTitle(title);
        course.setDescription("테스트 강좌 설명입니다.");
        course.setThumbnailUrl("course.png");
        course.setCreatedAt(LocalDateTime.now());

        return course;
    }

    private Lecture createLecture(
            Long lectureId,
            Course course,
            String title,
            String description,
            String videoUrl,
            String thumbnailUrl,
            LectureStatus status,
            Integer lectureOrder
    ) {
        Lecture lecture = new Lecture();
        lecture.setLectureId(lectureId);
        lecture.setCourse(course);
        lecture.setTitle(title);
        lecture.setDescription(description);
        lecture.setVideoUrl(videoUrl);
        lecture.setThumbnailUrl(thumbnailUrl);
        lecture.setStatus(status);
        lecture.setLectureOrder(lectureOrder);
        lecture.setCreatedAt(LocalDateTime.now());
        lecture.setUpdatedAt(LocalDateTime.now());

        return lecture;
    }
}
