package com.wanted.codebombalms.course.infrastructure.lecture;

import com.wanted.codebombalms.course.application.port.LectureManagementPort;
import com.wanted.codebombalms.lecture.application.port.LectureMaterialStoragePort;
import com.wanted.codebombalms.lecture.domain.model.LectureMaterial;
import com.wanted.codebombalms.lecture.domain.repository.LectureMaterialRepository;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
@Slf4j
public class LectureManagementAdapter implements LectureManagementPort {

    private final LectureRepository lectureRepository;
    private final LectureMaterialRepository lectureMaterialRepository;
    private final LectureMaterialStoragePort lectureMaterialStoragePort;

    @Override
    public void deleteLecturesByCourseId(Long courseId) {
        var lectures = lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId);
        var lectureIds = lectures.stream()
                .map(lecture -> lecture.getLectureId())
                .toList();

        if (!lectureIds.isEmpty()) {
            lectureMaterialRepository.findByLectureIdInAndDeletedAtIsNull(lectureIds)
                    .forEach(this::deleteMaterial);
        }

        lectures.forEach(lecture -> {
            lecture.delete();
            lectureRepository.save(lecture);
        });
    }

    private void deleteMaterial(LectureMaterial material) {
        String filePath = material.getFilePath();
        material.delete();
        lectureMaterialRepository.save(material);
        runAfterCommit(() -> deleteMaterialFileQuietly(filePath));
    }

    private void runAfterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }

    private void deleteMaterialFileQuietly(String filePath) {
        try {
            lectureMaterialStoragePort.delete(filePath);
        } catch (RuntimeException e) {
            log.warn("Failed to delete lecture material file after course deletion. filePath={}", filePath, e);
        }
    }
}
