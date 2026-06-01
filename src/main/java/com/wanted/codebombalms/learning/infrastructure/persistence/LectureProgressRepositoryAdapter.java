package com.wanted.codebombalms.learning.infrastructure.persistence;

import com.wanted.codebombalms.learning.domain.model.LectureProgress;
import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LectureProgressRepositoryAdapter implements LectureProgressRepository {

    private final SpringDataLectureProgressRepository springDataLectureProgressRepository;

    @Override
    public LectureProgress save(LectureProgress lectureProgress) {
        LectureProgressJpaEntity entity = lectureProgress.getLectureProgressId() == null
                ? LectureProgressJpaEntity.from(lectureProgress)
                : springDataLectureProgressRepository.findById(lectureProgress.getLectureProgressId())
                .map(found -> {
                    found.apply(lectureProgress);
                    return found;
                })
                .orElseGet(() -> LectureProgressJpaEntity.from(lectureProgress));

        return springDataLectureProgressRepository.save(entity).toDomain();
    }

    @Override
    public Optional<LectureProgress> findByUserIdAndLectureId(Long userId, Long lectureId) {
        return springDataLectureProgressRepository.findByUserIdAndLectureId(userId, lectureId)
                .map(LectureProgressJpaEntity::toDomain);
    }

    @Override
    public long countCompletedByUserIdAndLectureIds(Long userId, List<Long> lectureIds) {
        if (lectureIds.isEmpty()) {
            return 0;
        }
        return springDataLectureProgressRepository.countByUserIdAndLectureIdInAndCompletedTrue(userId, lectureIds);
    }

    @Override
    public long countCompletedByLectureId(Long lectureId) {
        return springDataLectureProgressRepository.countByLectureIdAndCompletedTrue(lectureId);
    }
}
