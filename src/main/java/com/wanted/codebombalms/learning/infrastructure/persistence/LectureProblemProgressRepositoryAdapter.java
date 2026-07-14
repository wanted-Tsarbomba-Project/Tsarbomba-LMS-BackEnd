package com.wanted.codebombalms.learning.infrastructure.persistence;

import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository.UserCourseKey;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LectureProblemProgressRepositoryAdapter implements LectureProblemProgressRepository {

    private final SpringDataLectureProblemProgressRepository springDataLectureProblemProgressRepository;

    @Override
    public LectureProblemProgress save(LectureProblemProgress lectureProblemProgress) {
        if (lectureProblemProgress.getLectureProblemProgressId() == null) {
            return insert(lectureProblemProgress);
        }

        LectureProblemProgressJpaEntity entity = springDataLectureProblemProgressRepository
                .findById(lectureProblemProgress.getLectureProblemProgressId())
                .map(found -> {
                    found.apply(lectureProblemProgress);
                    return found;
                })
                .orElseGet(() -> LectureProblemProgressJpaEntity.from(lectureProblemProgress));

        return springDataLectureProblemProgressRepository.save(entity).toDomain();
    }

    /**
     * 동일 사용자의 최초 진입 요청이 겹치면 두 요청 모두 기존 행이 없다고 판단해 동시에 INSERT를 시도할 수 있다.
     * 이 경우 나중 INSERT는 유니크 제약 위반으로 실패하므로, 먼저 성공한 행을 재조회해 그대로 반환한다.
     */
    private LectureProblemProgress insert(LectureProblemProgress lectureProblemProgress) {
        try {
            return springDataLectureProblemProgressRepository
                    .save(LectureProblemProgressJpaEntity.from(lectureProblemProgress))
                    .toDomain();
        } catch (DataIntegrityViolationException e) {
            return springDataLectureProblemProgressRepository
                    .findByUserIdAndLectureProblemSetId(
                            lectureProblemProgress.getUserId(),
                            lectureProblemProgress.getLectureProblemSetId()
                    )
                    .map(LectureProblemProgressJpaEntity::toDomain)
                    .orElseThrow(() -> e);
        }
    }

    @Override
    public Optional<LectureProblemProgress> findByUserIdAndLectureProblemSetId(
            Long userId,
            Long lectureProblemSetId
    ) {
        return springDataLectureProblemProgressRepository
                .findByUserIdAndLectureProblemSetId(userId, lectureProblemSetId)
                .map(LectureProblemProgressJpaEntity::toDomain);
    }

    @Override
    public Optional<LectureProblemProgress> findByUserIdAndLectureProblemSetIdForUpdate(
            Long userId,
            Long lectureProblemSetId
    ) {
        return springDataLectureProblemProgressRepository
                .findByUserIdAndLectureProblemSetIdForUpdate(userId, lectureProblemSetId)
                .map(LectureProblemProgressJpaEntity::toDomain);
    }

    @Override
    public long countCompletedByUserIdAndLectureProblemSetIds(Long userId, List<Long> lectureProblemSetIds) {
        if (lectureProblemSetIds.isEmpty()) {
            return 0;
        }
        return springDataLectureProblemProgressRepository
                .countByUserIdAndLectureProblemSetIdInAndCompletedTrue(userId, lectureProblemSetIds);
    }

    @Override
    public Map<Long, Long> countCompletedByUserIdsAndLectureProblemSetIds(
            List<Long> userIds,
            List<Long> lectureProblemSetIds
    ) {
        if (userIds.isEmpty() || lectureProblemSetIds.isEmpty()) {
            return Map.of();
        }
        return springDataLectureProblemProgressRepository
                .countCompletedByUserIdsAndLectureProblemSetIds(userIds, lectureProblemSetIds)
                .stream()
                .collect(Collectors.toMap(
                        SpringDataLectureProblemProgressRepository.UserCompletedProblemSetCount::getUserId,
                        SpringDataLectureProblemProgressRepository.UserCompletedProblemSetCount::getCompletedCount
                ));
    }

    @Override
    public Map<UserCourseKey, Long> countCompletedMainByUserIdsAndCourseIds(
            Collection<Long> userIds,
            Collection<Long> courseIds
    ) {
        if (userIds.isEmpty() || courseIds.isEmpty()) {
            return Map.of();
        }
        return springDataLectureProblemProgressRepository.countCompletedMainByUserIdsAndCourseIds(userIds, courseIds)
                .stream()
                .collect(Collectors.toMap(
                        count -> new UserCourseKey(count.getUserId(), count.getCourseId()),
                        SpringDataLectureProblemProgressRepository.UserCourseCompletedProblemSetCount::getCompletedCount
                ));
    }

    @Override
    public long countCompletedByLectureProblemSetIds(List<Long> lectureProblemSetIds) {
        if (lectureProblemSetIds.isEmpty()) {
            return 0;
        }
        return springDataLectureProblemProgressRepository
                .countByLectureProblemSetIdInAndCompletedTrue(lectureProblemSetIds);
    }
}
