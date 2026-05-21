package com.wanted.codebombalms.domain.lecture.application.service;

import com.wanted.codebombalms.domain.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.domain.lecture.domain.model.Lecture;
import com.wanted.codebombalms.domain.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.domain.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.domain.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureCreateRequest;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureUpdateRequest;
import com.wanted.codebombalms.domain.lecture.presentation.api.response.LectureDetailResponse;
import com.wanted.codebombalms.domain.lecture.presentation.api.response.LectureResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("LectureService unit test")
class LectureServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private LectureService lectureService;

    @Test
    void createLecture_returnsResponse() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java");
        Lecture savedLecture = createLecture(1L, course, "Java 1", LectureStatus.ACTIVE, 1);

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));
        given(lectureRepository.save(any(Lecture.class))).willReturn(savedLecture);

        LectureDetailResponse response = lectureService.createLecture(
                courseId,
                new LectureCreateRequest("Java 1", "description", "java-1.mp4", "java-1.png", 1, LectureStatus.ACTIVE)
        );

        assertEquals(1L, response.getLectureId());
        assertEquals(courseId, response.getCourseId());
        assertEquals(10L, response.getInstructorId());
        assertEquals("Java 1", response.getTitle());
    }

    @Test
    void createLecture_throwsNotFound_whenCourseMissing() {
        Long courseId = 999L;
        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> lectureService.createLecture(
                        courseId,
                        new LectureCreateRequest("Java 1", "description", "java-1.mp4", "java-1.png", 1, LectureStatus.ACTIVE)
                )
        );

        assertEquals(CourseErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void findLecturesByCourseId_returnsOrderedLectures() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java");
        Lecture lecture = createLecture(1L, course, "Java 1", LectureStatus.ACTIVE, 1);

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));
        given(lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId))
                .willReturn(List.of(lecture));

        List<LectureResponse> responses = lectureService.findLecturesByCourseId(courseId);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getLectureId());
        assertEquals("Java 1", responses.get(0).getTitle());
    }

    @Test
    void findLectureById_throwsNotFound_whenMissing() {
        Long lectureId = 999L;
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)).willReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> lectureService.findLectureById(lectureId)
        );

        assertEquals(LectureErrorCode.LECTURE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateLecture_updatesAndSavesLecture() {
        Long lectureId = 1L;
        Course course = createCourse(1L, 10L, "Java");
        Lecture lecture = createLecture(lectureId, course, "Java 1", LectureStatus.ACTIVE, 1);

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)).willReturn(Optional.of(lecture));
        given(lectureRepository.save(lecture)).willReturn(lecture);

        LectureDetailResponse response = lectureService.updateLecture(
                lectureId,
                new LectureUpdateRequest("Updated Java", "updated", "updated.mp4", "updated.png", 2, LectureStatus.INACTIVE)
        );

        assertEquals("Updated Java", response.getTitle());
        assertEquals(LectureStatus.INACTIVE, response.getStatus());
        verify(lectureRepository).save(lecture);
    }

    @Test
    void deleteLecture_deletesAndSavesLecture() {
        Long lectureId = 1L;
        Lecture lecture = createLecture(lectureId, createCourse(1L, 10L, "Java"), "Java 1", LectureStatus.ACTIVE, 1);

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)).willReturn(Optional.of(lecture));
        given(lectureRepository.save(lecture)).willReturn(lecture);

        lectureService.deleteLecture(lectureId);

        assertEquals(LectureStatus.DELETED, lecture.getStatus());
        assertNotNull(lecture.getDeletedAt());
        verify(lectureRepository).save(lecture);
    }

    private Course createCourse(Long courseId, Long instructorId, String title) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setInstructorId(instructorId);
        course.setTitle(title);
        course.setDescription("course description");
        course.setThumbnailUrl("course.png");
        course.setCreatedAt(LocalDateTime.now());
        return course;
    }

    private Lecture createLecture(
            Long lectureId,
            Course course,
            String title,
            LectureStatus status,
            Integer lectureOrder
    ) {
        Lecture lecture = new Lecture();
        lecture.setLectureId(lectureId);
        lecture.setCourse(course);
        lecture.setTitle(title);
        lecture.setDescription("lecture description");
        lecture.setVideoUrl("video.mp4");
        lecture.setThumbnailUrl("lecture.png");
        lecture.setStatus(status);
        lecture.setLectureOrder(lectureOrder);
        lecture.setCreatedAt(LocalDateTime.now());
        lecture.setUpdatedAt(LocalDateTime.now());
        return lecture;
    }
}
