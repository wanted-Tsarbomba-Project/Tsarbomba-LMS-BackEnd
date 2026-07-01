package com.wanted.codebombalms.course.infrastructure.lecture;

import com.wanted.codebombalms.course.application.port.LectureManagementPort;
import com.wanted.codebombalms.lecture.application.port.LectureMaterialStoragePort;
import com.wanted.codebombalms.lecture.domain.model.LectureMaterial;
import com.wanted.codebombalms.lecture.domain.repository.LectureMaterialRepository;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureManagementAdapter implements LectureManagementPort {

    private final LectureRepository lectureRepository;
    private final LectureMaterialRepository lectureMaterialRepository;
    private final LectureMaterialStoragePort lectureMaterialStoragePort;

    @Override
    public void deleteLecturesByCourseId(Long courseId) {
        lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId)
                .forEach(lecture -> {
                    deleteMaterialsByLectureId(lecture.getLectureId());
                    lecture.delete();
                    lectureRepository.save(lecture);
                });
    }

    private void deleteMaterialsByLectureId(Long lectureId) {
        lectureMaterialRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .forEach(this::deleteMaterial);
    }

    private void deleteMaterial(LectureMaterial material) {
        material.delete();
        lectureMaterialRepository.save(material);
        lectureMaterialStoragePort.delete(material.getFilePath());
    }
}
