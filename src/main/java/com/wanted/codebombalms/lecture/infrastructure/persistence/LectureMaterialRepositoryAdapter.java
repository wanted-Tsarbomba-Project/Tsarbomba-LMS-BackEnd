package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.lecture.domain.model.LectureMaterial;
import com.wanted.codebombalms.lecture.domain.repository.LectureMaterialRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LectureMaterialRepositoryAdapter implements LectureMaterialRepository {

    private final SpringDataLectureMaterialRepository springDataLectureMaterialRepository;

    @Override
    public LectureMaterial save(LectureMaterial material) {
        LectureMaterialJpaEntity entity = material.getLectureMaterialId() == null
                ? LectureMaterialJpaEntity.from(material)
                : springDataLectureMaterialRepository.findById(material.getLectureMaterialId())
                .map(found -> {
                    found.apply(material);
                    return found;
                })
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_MATERIAL_NOT_FOUND));

        return springDataLectureMaterialRepository.save(entity).toDomain();
    }

    @Override
    public List<LectureMaterial> findByLectureIdAndDeletedAtIsNull(Long lectureId) {
        return springDataLectureMaterialRepository
                .findByLectureIdAndDeletedAtIsNullOrderByCreatedAtDesc(lectureId)
                .stream()
                .map(LectureMaterialJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<LectureMaterial> findByLectureMaterialIdAndDeletedAtIsNull(Long lectureMaterialId) {
        return springDataLectureMaterialRepository.findByLectureMaterialIdAndDeletedAtIsNull(lectureMaterialId)
                .map(LectureMaterialJpaEntity::toDomain);
    }
}
