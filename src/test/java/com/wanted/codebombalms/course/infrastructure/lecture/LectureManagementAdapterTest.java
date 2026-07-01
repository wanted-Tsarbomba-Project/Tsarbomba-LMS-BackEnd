package com.wanted.codebombalms.course.infrastructure.lecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.lecture.application.port.LectureMaterialStoragePort;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureMaterial;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.domain.repository.LectureMaterialRepository;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LectureManagementAdapter unit test")
class LectureManagementAdapterTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private LectureMaterialRepository lectureMaterialRepository;

    @Mock
    private LectureMaterialStoragePort lectureMaterialStoragePort;

    @InjectMocks
    private LectureManagementAdapter lectureManagementAdapter;

    @Test
    void deleteLecturesByCourseId_deletesLectureMaterialsBeforeLectures() {
        Long courseId = 1L;
        Lecture lecture = createLecture(10L, courseId);
        LectureMaterial material = createMaterial(100L, lecture.getLectureId());

        given(lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId))
                .willReturn(List.of(lecture));
        given(lectureMaterialRepository.findByLectureIdAndDeletedAtIsNull(lecture.getLectureId()))
                .willReturn(List.of(material));

        lectureManagementAdapter.deleteLecturesByCourseId(courseId);

        assertNotNull(material.getDeletedAt());
        assertEquals(LectureStatus.DELETED, lecture.getStatus());
        assertNotNull(lecture.getDeletedAt());

        InOrder inOrder = inOrder(
                lectureMaterialRepository,
                lectureMaterialStoragePort,
                lectureRepository
        );
        inOrder.verify(lectureMaterialRepository).save(material);
        inOrder.verify(lectureMaterialStoragePort).delete("lecture_materials/stored-guide.pdf");
        inOrder.verify(lectureRepository).save(lecture);
    }

    private Lecture createLecture(Long lectureId, Long courseId) {
        Course course = new Course();
        course.setCourseId(courseId);

        return Lecture.restore(
                lectureId,
                course,
                "Java lecture",
                "description",
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                "thumbnail.png",
                null,
                LectureStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                1
        );
    }

    private LectureMaterial createMaterial(Long lectureMaterialId, Long lectureId) {
        return LectureMaterial.restore(
                lectureMaterialId,
                lectureId,
                "guide.pdf",
                "stored-guide.pdf",
                "lecture_materials/stored-guide.pdf",
                "application/pdf",
                1024L,
                LocalDateTime.now(),
                null
        );
    }
}
