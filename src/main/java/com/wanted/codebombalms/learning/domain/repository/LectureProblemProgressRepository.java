package com.wanted.codebombalms.learning.domain.repository;

import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LectureProblemProgressRepository {

    LectureProblemProgress save(LectureProblemProgress lectureProblemProgress);

    Optional<LectureProblemProgress> findByUserIdAndLectureProblemSetId(Long userId, Long lectureProblemSetId);

    Optional<LectureProblemProgress> findByUserIdAndLectureProblemSetIdForUpdate(
            Long userId,
            Long lectureProblemSetId
    );

    long countCompletedByUserIdAndLectureProblemSetIds(Long userId, List<Long> lectureProblemSetIds);

    Map<Long, Long> countCompletedByUserIdsAndLectureProblemSetIds(
            List<Long> userIds,
            List<Long> lectureProblemSetIds
    );

    long countCompletedByLectureProblemSetIds(List<Long> lectureProblemSetIds);
}
