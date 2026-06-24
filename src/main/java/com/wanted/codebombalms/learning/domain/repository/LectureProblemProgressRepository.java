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

    /**
     * Returns completed lecture problem set counts grouped by user id.
     * User ids without completed problem sets are omitted, so callers should treat missing values as 0.
     */
    Map<Long, Long> countCompletedByUserIdsAndLectureProblemSetIds(
            List<Long> userIds,
            List<Long> lectureProblemSetIds
    );

    long countCompletedByLectureProblemSetIds(List<Long> lectureProblemSetIds);
}
