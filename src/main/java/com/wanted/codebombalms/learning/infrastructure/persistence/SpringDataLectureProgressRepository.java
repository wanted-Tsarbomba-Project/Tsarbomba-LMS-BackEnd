package com.wanted.codebombalms.learning.infrastructure.persistence;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLectureProgressRepository extends JpaRepository<LectureProgressJpaEntity, Long> {

    Optional<LectureProgressJpaEntity> findByUserIdAndLectureId(Long userId, Long lectureId);

    long countByUserIdAndLectureIdInAndCompletedTrue(Long userId, Collection<Long> lectureIds);
}
