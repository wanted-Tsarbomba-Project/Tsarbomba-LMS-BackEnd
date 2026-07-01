package com.wanted.codebombalms.lecture.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLectureMaterialRepository extends JpaRepository<LectureMaterialJpaEntity, Long> {

    List<LectureMaterialJpaEntity> findByLectureIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long lectureId);

    List<LectureMaterialJpaEntity> findByLectureIdInAndDeletedAtIsNull(List<Long> lectureIds);

    Optional<LectureMaterialJpaEntity> findByLectureMaterialIdAndDeletedAtIsNull(Long lectureMaterialId);
}
