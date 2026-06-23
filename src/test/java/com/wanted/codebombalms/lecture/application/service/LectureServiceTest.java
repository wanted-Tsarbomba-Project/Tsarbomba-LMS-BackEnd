package com.wanted.codebombalms.lecture.application.service;

import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.lecture.application.command.CreateLectureCommand;
import com.wanted.codebombalms.lecture.application.command.UpdateLectureCommand;
import com.wanted.codebombalms.lecture.application.policy.LectureCreationPolicy;
import com.wanted.codebombalms.lecture.application.port.CourseCatalogPort;
import com.wanted.codebombalms.lecture.application.policy.LectureAccessPolicy;
import com.wanted.codebombalms.lecture.application.service.LectureCommandService;
import com.wanted.codebombalms.lecture.application.service.LectureQueryService;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Lecture application service unit test")
class LectureServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private CourseCatalogPort courseCatalogPort;

    @Mock
    private LectureCreationPolicy lectureCreationPolicy;

    @Mock
    private LectureAccessPolicy lectureAccessPolicy;

    @InjectMocks
    private LectureCommandService lectureCommandService;

    @InjectMocks
    private LectureQueryService lectureQueryService;

    @Test
    void createLecture_returnsLecture() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java");
        Lecture savedLecture = createLecture(1L, course, "Java 1", LectureStatus.ACTIVE, 1);

        given(courseCatalogPort.findCourse(courseId)).willReturn(course);
        given(lectureRepository.save(any(Lecture.class))).willReturn(savedLecture);

        Lecture result = lectureCommandService.createLecture(
                new CreateLectureCommand(courseId, "Java 1", "description", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "java-1.png", null, 1, LectureStatus.ACTIVE)
        );

        assertEquals(1L, result.getLectureId());
        assertEquals(courseId, result.getCourse().getCourseId());
        assertEquals("Java 1", result.getTitle());
        verify(lectureCreationPolicy).validate(course);
    }

    @Test
    void createLecture_throwsValidation_whenVideoUrlIsNotYoutube() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java");

        given(courseCatalogPort.findCourse(courseId)).willReturn(course);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureCommandService.createLecture(
                        new CreateLectureCommand(courseId, "Java 1", "description", "java-1.mp4", "java-1.png", null, 1, LectureStatus.ACTIVE)
                )
        );

        assertEquals(LectureErrorCode.INVALID_YOUTUBE_VIDEO_URL, exception.getErrorCode());
    }

    @Test
    void createLecture_throwsValidation_whenVideoUrlIsBlank() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java");

        given(courseCatalogPort.findCourse(courseId)).willReturn(course);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureCommandService.createLecture(
                        new CreateLectureCommand(courseId, "Java 1", "description", "   ", "java-1.png", null, 1, LectureStatus.ACTIVE)
                )
        );

        assertEquals(LectureErrorCode.INVALID_YOUTUBE_VIDEO_URL, exception.getErrorCode());
    }

    @Test
    void createLecture_throwsValidation_whenYoutubeVideoIdIsInvalid() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java");

        given(courseCatalogPort.findCourse(courseId)).willReturn(course);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureCommandService.createLecture(
                        new CreateLectureCommand(
                                courseId,
                                "Java 1",
                                "description",
                                "https://www.youtube.com/watch?v=short",
                                "java-1.png",
                                null,
                                1,
                                LectureStatus.ACTIVE
                        )
                )
        );

        assertEquals(LectureErrorCode.INVALID_YOUTUBE_VIDEO_URL, exception.getErrorCode());
    }

    @Test
    void createLecture_throwsValidation_whenWatchUrlHasMultipleVideoIds() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java");

        given(courseCatalogPort.findCourse(courseId)).willReturn(course);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureCommandService.createLecture(
                        new CreateLectureCommand(
                                courseId,
                                "Java 1",
                                "description",
                                "https://www.youtube.com/watch?v=short&v=dQw4w9WgXcQ",
                                "java-1.png",
                                null,
                                1,
                                LectureStatus.ACTIVE
                        )
                )
        );

        assertEquals(LectureErrorCode.INVALID_YOUTUBE_VIDEO_URL, exception.getErrorCode());
    }

    @Test
    void createLecture_throwsValidation_whenYoutubeVideoIdHasNonAsciiCharacters() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java");

        given(courseCatalogPort.findCourse(courseId)).willReturn(course);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureCommandService.createLecture(
                        new CreateLectureCommand(
                                courseId,
                                "Java 1",
                                "description",
                                "https://www.youtube.com/watch?v=가나다라마바사아자차카",
                                "java-1.png",
                                null,
                                1,
                                LectureStatus.ACTIVE
                        )
                )
        );

        assertEquals(LectureErrorCode.INVALID_YOUTUBE_VIDEO_URL, exception.getErrorCode());
    }

    @Test
    void createLecture_throwsValidation_whenYoutubeUrlHasPort() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java");

        given(courseCatalogPort.findCourse(courseId)).willReturn(course);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureCommandService.createLecture(
                        new CreateLectureCommand(
                                courseId,
                                "Java 1",
                                "description",
                                "https://www.youtube.com:444/watch?v=dQw4w9WgXcQ",
                                "java-1.png",
                                null,
                                1,
                                LectureStatus.ACTIVE
                        )
                )
        );

        assertEquals(LectureErrorCode.INVALID_YOUTUBE_VIDEO_URL, exception.getErrorCode());
    }

    @Test
    void createLecture_throwsValidation_whenYoutubeUrlHasUserInfo() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java");

        given(courseCatalogPort.findCourse(courseId)).willReturn(course);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureCommandService.createLecture(
                        new CreateLectureCommand(
                                courseId,
                                "Java 1",
                                "description",
                                "https://user@www.youtube.com/watch?v=dQw4w9WgXcQ",
                                "java-1.png",
                                null,
                                1,
                                LectureStatus.ACTIVE
                        )
                )
        );

        assertEquals(LectureErrorCode.INVALID_YOUTUBE_VIDEO_URL, exception.getErrorCode());
    }

    @Test
    void createLecture_acceptsYoutubeShortsUrl() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java");
        Lecture savedLecture = createLecture(1L, course, "Java 1", LectureStatus.ACTIVE, 1);

        given(courseCatalogPort.findCourse(courseId)).willReturn(course);
        given(lectureRepository.save(any(Lecture.class))).willReturn(savedLecture);

        Lecture result = lectureCommandService.createLecture(
                new CreateLectureCommand(
                        courseId,
                        "Java 1",
                        "description",
                        "https://www.youtube.com/shorts/dQw4w9WgXcQ",
                        "java-1.png",
                        null,
                        1,
                        LectureStatus.ACTIVE
                )
        );

        assertEquals(1L, result.getLectureId());
        verify(lectureRepository).save(any(Lecture.class));
    }

    @Test
    void createLecture_throwsNotFound_whenCourseMissing() {
        Long courseId = 999L;
        given(courseCatalogPort.findCourse(courseId))
                .willThrow(new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> lectureCommandService.createLecture(
                        new CreateLectureCommand(courseId, "Java 1", "description", "https://youtu.be/dQw4w9WgXcQ", "java-1.png", null, 1, LectureStatus.ACTIVE)
                )
        );

        assertEquals(CourseErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void findLecturesByCourseId_returnsSummaries() {
        Long courseId = 1L;
        Course course = createCourse(courseId, 10L, "Java");
        Lecture lecture = createLecture(1L, course, "Java 1", LectureStatus.ACTIVE, 1);

        given(courseCatalogPort.findCourse(courseId)).willReturn(course);
        given(lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId))
                .willReturn(List.of(lecture));

        List<Lecture> results = lectureQueryService.findLecturesByCourseId(courseId);

        assertEquals(1, results.size());
        assertEquals("Java 1", results.get(0).getTitle());
    }

    @Test
    void findLectureById_throwsNotFound_whenMissing() {
        Long lectureId = 999L;
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)).willReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> lectureQueryService.findLectureById(lectureId)
        );

        assertEquals(LectureErrorCode.LECTURE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void findLectureByIdForLearning_allowsFirstLectureWithoutPreviousProgressCheck() {
        Long lectureId = 1L;
        Long userId = 10L;
        Course course = createCourse(1L, 20L, "Java");
        Lecture lecture = createLecture(lectureId, course, "Java 1", LectureStatus.ACTIVE, 1);

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)).willReturn(Optional.of(lecture));
        given(lectureRepository.findPreviousLectureIds(course.getCourseId(), lecture.getLectureOrder()))
                .willReturn(List.of());

        Lecture result = lectureQueryService.findLectureByIdForLearning(lectureId, userId, false);

        assertEquals(lectureId, result.getLectureId());
        verify(lectureAccessPolicy).validateLearningContentAccess(lecture, userId, false);
        verify(lectureAccessPolicy).validatePreviousLecturesCompleted(userId, List.of());
    }

    @Test
    void findLectureByIdForLearning_validatesPreviousLecturesWithinSameCourse() {
        Long lectureId = 3L;
        Long userId = 10L;
        Course course = createCourse(1L, 20L, "Java");
        Lecture lecture = createLecture(lectureId, course, "Java 3", LectureStatus.ACTIVE, 3);
        List<Long> previousLectureIds = List.of(1L, 2L);

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)).willReturn(Optional.of(lecture));
        given(lectureRepository.findPreviousLectureIds(course.getCourseId(), lecture.getLectureOrder()))
                .willReturn(previousLectureIds);

        Lecture result = lectureQueryService.findLectureByIdForLearning(lectureId, userId, false);

        assertEquals(lectureId, result.getLectureId());
        verify(lectureAccessPolicy).validateLearningContentAccess(lecture, userId, false);
        verify(lectureAccessPolicy).validatePreviousLecturesCompleted(userId, previousLectureIds);
    }

    @Test
    void findLectureByIdForLearning_skipsPreviousProgressCheckForOperator() {
        Long lectureId = 3L;
        Course course = createCourse(1L, 20L, "Java");
        Lecture lecture = createLecture(lectureId, course, "Java 3", LectureStatus.ACTIVE, 3);

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)).willReturn(Optional.of(lecture));

        Lecture result = lectureQueryService.findLectureByIdForLearning(lectureId, null, true);

        assertEquals(lectureId, result.getLectureId());
        verify(lectureAccessPolicy).validateLearningContentAccess(lecture, null, true);
        verify(lectureRepository, never()).findPreviousLectureIds(eq(course.getCourseId()), any());
    }

    @Test
    void updateLecture_updatesAndSavesLecture() {
        Long lectureId = 1L;
        Course course = createCourse(1L, 10L, "Java");
        Lecture lecture = createLecture(lectureId, course, "Java 1", LectureStatus.ACTIVE, 1);

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)).willReturn(Optional.of(lecture));
        given(lectureRepository.save(lecture)).willReturn(lecture);

        Lecture result = lectureCommandService.updateLecture(
                new UpdateLectureCommand(lectureId, "Updated Java", "updated", "https://www.youtube.com/embed/dQw4w9WgXcQ", "updated.png", null, 2, LectureStatus.INACTIVE)
        );

        assertEquals("Updated Java", result.getTitle());
        assertEquals(LectureStatus.INACTIVE, result.getStatus());
        verify(lectureRepository).save(lecture);
    }

    @Test
    void updateLecture_throwsValidation_whenVideoUrlIsNotYoutube() {
        Long lectureId = 1L;
        Lecture lecture = createLecture(lectureId, createCourse(1L, 10L, "Java"), "Java 1", LectureStatus.ACTIVE, 1);

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)).willReturn(Optional.of(lecture));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureCommandService.updateLecture(
                        new UpdateLectureCommand(
                                lectureId,
                                "Updated Java",
                                "updated",
                                "https://example.com/video.mp4",
                                "updated.png",
                                null,
                                2,
                                LectureStatus.INACTIVE
                        )
                )
        );

        assertEquals(LectureErrorCode.INVALID_YOUTUBE_VIDEO_URL, exception.getErrorCode());
    }

    @Test
    void updateLecture_throwsValidation_whenVideoUrlIsBlank() {
        Long lectureId = 1L;
        Lecture lecture = createLecture(lectureId, createCourse(1L, 10L, "Java"), "Java 1", LectureStatus.ACTIVE, 1);

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)).willReturn(Optional.of(lecture));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureCommandService.updateLecture(
                        new UpdateLectureCommand(
                                lectureId,
                                "Updated Java",
                                "updated",
                                "   ",
                                "updated.png",
                                null,
                                2,
                                LectureStatus.INACTIVE
                        )
                )
        );

        assertEquals(LectureErrorCode.INVALID_YOUTUBE_VIDEO_URL, exception.getErrorCode());
    }

    @Test
    void updateLecture_throwsValidation_whenDeletingByStatus() {
        Long lectureId = 1L;
        Lecture lecture = createLecture(lectureId, createCourse(1L, 10L, "Java"), "Java 1", LectureStatus.ACTIVE, 1);

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)).willReturn(Optional.of(lecture));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureCommandService.updateLecture(new UpdateLectureCommand(lectureId, null, null, null, null, null, null, LectureStatus.DELETED))
        );

        assertEquals(LectureErrorCode.LECTURE_DELETE_STATUS_REQUIRES_DELETE, exception.getErrorCode());
    }

    @Test
    void deleteLecture_deletesAndSavesLecture() {
        Long lectureId = 1L;
        Lecture lecture = createLecture(lectureId, createCourse(1L, 10L, "Java"), "Java 1", LectureStatus.ACTIVE, 1);

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)).willReturn(Optional.of(lecture));
        given(lectureRepository.save(lecture)).willReturn(lecture);

        lectureCommandService.deleteLecture(lectureId);

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

    private Lecture createLecture(Long lectureId, Course course, String title, LectureStatus status, Integer lectureOrder) {
        Lecture lecture = new Lecture();
        lecture.setLectureId(lectureId);
        lecture.setCourse(course);
        lecture.setTitle(title);
        lecture.setDescription("lecture description");
        lecture.setVideoUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        lecture.setThumbnailUrl("lecture.png");
        lecture.setStatus(status);
        lecture.setLectureOrder(lectureOrder);
        lecture.setCreatedAt(LocalDateTime.now());
        lecture.setUpdatedAt(LocalDateTime.now());
        return lecture;
    }
}
