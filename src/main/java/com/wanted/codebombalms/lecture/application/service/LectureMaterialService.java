package com.wanted.codebombalms.lecture.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.lecture.application.command.UploadLectureMaterialCommand;
import com.wanted.codebombalms.lecture.application.port.LectureEnrollmentPort;
import com.wanted.codebombalms.lecture.application.port.LectureMaterialStoragePort;
import com.wanted.codebombalms.lecture.application.usecase.LectureMaterialUseCase;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureMaterial;
import com.wanted.codebombalms.lecture.domain.repository.LectureMaterialRepository;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LectureMaterialService implements LectureMaterialUseCase {

    private final LectureMaterialRepository lectureMaterialRepository;
    private final LectureRepository lectureRepository;
    private final LectureMaterialStoragePort lectureMaterialStoragePort;
    private final LectureEnrollmentPort lectureEnrollmentPort;

    @Override
    public LectureMaterial uploadMaterial(UploadLectureMaterialCommand command) {
        validateLecture(command.lectureId());

        var storedMaterial = lectureMaterialStoragePort.upload(
                command.originalFileName(),
                command.contentType(),
                command.fileSize(),
                command.content()
        );

        LectureMaterial material = LectureMaterial.create(
                command.lectureId(),
                storedMaterial.originalFileName(),
                storedMaterial.storedFileName(),
                storedMaterial.filePath(),
                storedMaterial.contentType(),
                storedMaterial.fileSize()
        );

        try {
            return lectureMaterialRepository.save(material);
        } catch (RuntimeException e) {
            deleteUploadedMaterialQuietly(storedMaterial.filePath(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureMaterial> findMaterials(Long lectureId) {
        validateLecture(lectureId);
        return lectureMaterialRepository.findByLectureIdAndDeletedAtIsNull(lectureId);
    }

    @Override
    @Transactional(readOnly = true)
    public String issueDownloadUrl(Long lectureMaterialId, Long userId, boolean operator) {
        LectureMaterial material = findMaterial(lectureMaterialId);

        if (!operator) {
            if (userId == null) {
                throw new ForbiddenException(LectureErrorCode.LECTURE_MATERIAL_ACCESS_DENIED);
            }

            Lecture lecture = validateLecture(material.getLectureId());
            Long courseId = lecture.getCourse().getCourseId();
            if (!lectureEnrollmentPort.isActiveStudentOfCourse(courseId, userId)) {
                throw new ForbiddenException(LectureErrorCode.LECTURE_MATERIAL_ACCESS_DENIED);
            }
        }

        return lectureMaterialStoragePort.generateDownloadUrl(
                material.getFilePath(),
                material.getOriginalFileName()
        );
    }

    @Override
    public void deleteMaterial(Long lectureMaterialId) {
        LectureMaterial material = findMaterial(lectureMaterialId);
        material.delete();
        lectureMaterialRepository.save(material);
        lectureMaterialStoragePort.delete(material.getFilePath());
    }

    private Lecture validateLecture(Long lectureId) {
        return lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_NOT_FOUND));
    }

    private LectureMaterial findMaterial(Long lectureMaterialId) {
        return lectureMaterialRepository.findByLectureMaterialIdAndDeletedAtIsNull(lectureMaterialId)
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_MATERIAL_NOT_FOUND));
    }

    private void deleteUploadedMaterialQuietly(String filePath, RuntimeException originalException) {
        try {
            lectureMaterialStoragePort.delete(filePath);
        } catch (RuntimeException deleteException) {
            originalException.addSuppressed(deleteException);
            log.warn("Failed to delete uploaded lecture material after DB save failure. filePath={}", filePath, deleteException);
        }
    }
}
