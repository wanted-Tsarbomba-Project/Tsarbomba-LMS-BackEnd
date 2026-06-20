package com.wanted.codebombalms.lecture.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.lecture.application.command.UploadLectureMaterialCommand;
import com.wanted.codebombalms.lecture.application.port.LectureEnrollmentPort;
import com.wanted.codebombalms.lecture.application.port.LectureMaterialStoragePort;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureMaterial;
import com.wanted.codebombalms.lecture.domain.repository.LectureMaterialRepository;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LectureMaterialService unit test")
class LectureMaterialServiceTest {

    @Mock
    private LectureMaterialRepository lectureMaterialRepository;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private LectureMaterialStoragePort lectureMaterialStoragePort;

    @Mock
    private LectureEnrollmentPort lectureEnrollmentPort;

    @InjectMocks
    private LectureMaterialService lectureMaterialService;

    @Test
    void uploadMaterial_savesStoredMaterial() {
        Long lectureId = 1L;
        LectureMaterial saved = createMaterial(10L, lectureId);

        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId))
                .willReturn(Optional.of(createLecture(lectureId, 100L)));
        given(lectureMaterialStoragePort.upload("guide.pdf", "application/pdf", 3L, "pdf".getBytes()))
                .willReturn(new LectureMaterialStoragePort.StoredLectureMaterial(
                        "guide.pdf",
                        "stored-guide.pdf",
                        "lecture_materials/stored-guide.pdf",
                        "application/pdf",
                        3L
                ));
        given(lectureMaterialRepository.save(any(LectureMaterial.class))).willReturn(saved);

        LectureMaterial result = lectureMaterialService.uploadMaterial(
                new UploadLectureMaterialCommand(
                        lectureId,
                        "guide.pdf",
                        "application/pdf",
                        3L,
                        "pdf".getBytes()
                )
        );

        assertEquals(10L, result.getLectureMaterialId());
        assertEquals("guide.pdf", result.getOriginalFileName());
        verify(lectureMaterialRepository).save(any(LectureMaterial.class));
    }

    @Test
    void issueDownloadUrl_returnsUrl_whenStudentEnrolled() {
        Long lectureMaterialId = 10L;
        Long lectureId = 1L;
        Long userId = 20L;
        Long courseId = 100L;

        given(lectureMaterialRepository.findByLectureMaterialIdAndDeletedAtIsNull(lectureMaterialId))
                .willReturn(Optional.of(createMaterial(lectureMaterialId, lectureId)));
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId))
                .willReturn(Optional.of(createLecture(lectureId, courseId)));
        given(lectureEnrollmentPort.isActiveStudentOfCourse(courseId, userId)).willReturn(true);
        given(lectureMaterialStoragePort.generateDownloadUrl(
                "lecture_materials/stored-guide.pdf",
                "guide.pdf"
        )).willReturn("https://download-url");

        String result = lectureMaterialService.issueDownloadUrl(lectureMaterialId, userId, false);

        assertEquals("https://download-url", result);
    }

    @Test
    void issueDownloadUrl_throwsForbidden_whenStudentNotEnrolled() {
        Long lectureMaterialId = 10L;
        Long lectureId = 1L;
        Long userId = 20L;
        Long courseId = 100L;

        given(lectureMaterialRepository.findByLectureMaterialIdAndDeletedAtIsNull(lectureMaterialId))
                .willReturn(Optional.of(createMaterial(lectureMaterialId, lectureId)));
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId))
                .willReturn(Optional.of(createLecture(lectureId, courseId)));
        given(lectureEnrollmentPort.isActiveStudentOfCourse(courseId, userId)).willReturn(false);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> lectureMaterialService.issueDownloadUrl(lectureMaterialId, userId, false)
        );

        assertEquals(LectureErrorCode.LECTURE_MATERIAL_ACCESS_DENIED, exception.getErrorCode());
    }

    private Lecture createLecture(Long lectureId, Long courseId) {
        Course course = new Course();
        course.setCourseId(courseId);

        Lecture lecture = new Lecture();
        lecture.setLectureId(lectureId);
        lecture.setCourse(course);
        return lecture;
    }

    private LectureMaterial createMaterial(Long lectureMaterialId, Long lectureId) {
        LectureMaterial material = new LectureMaterial();
        material.setLectureMaterialId(lectureMaterialId);
        material.setLectureId(lectureId);
        material.setOriginalFileName("guide.pdf");
        material.setStoredFileName("stored-guide.pdf");
        material.setFilePath("lecture_materials/stored-guide.pdf");
        material.setContentType("application/pdf");
        material.setFileSize(3L);
        return material;
    }
}
