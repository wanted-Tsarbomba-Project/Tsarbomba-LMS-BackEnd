package com.wanted.codebombalms.learning.infrastructure.persistence;

import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LectureProblemProgressRepositoryAdapter implements LectureProblemProgressRepository {

    private final SpringDataLectureProblemProgressRepository springDataLectureProblemProgressRepository;

    @Override
    public LectureProblemProgress save(LectureProblemProgress lectureProblemProgress) {
        LectureProblemProgressJpaEntity entity = lectureProblemProgress.getLectureProblemProgressId() == null
                ? LectureProblemProgressJpaEntity.from(lectureProblemProgress)
                : springDataLectureProblemProgressRepository.findById(
                        lectureProblemProgress.getLectureProblemProgressId()
                )
                .map(found -> {
                    found.apply(lectureProblemProgress);
                    return found;
                })
                .orElseGet(() -> LectureProblemProgressJpaEntity.from(lectureProblemProgress));

        return springDataLectureProblemProgressRepository.save(entity).toDomain();
    }

    @Override
    public Optional<LectureProblemProgress> findByUserIdAndCourseProblemStepId(
            Long userId,
            Long courseProblemStepId
    ) {
        return springDataLectureProblemProgressRepository
                .findByUserIdAndCourseProblemStepId(userId, courseProblemStepId)
                .map(LectureProblemProgressJpaEntity::toDomain);
    }

    @Override
    public long countCompletedByUserIdAndCourseProblemStepIds(Long userId, List<Long> courseProblemStepIds) {
        if (courseProblemStepIds.isEmpty()) {
            return 0;
        }
        return springDataLectureProblemProgressRepository
                .countByUserIdAndCourseProblemStepIdInAndCompletedTrue(userId, courseProblemStepIds);
    }
}
