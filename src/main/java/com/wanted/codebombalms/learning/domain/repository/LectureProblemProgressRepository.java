package com.wanted.codebombalms.learning.domain.repository;

import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import java.util.List;
import java.util.Optional;

public interface LectureProblemProgressRepository {

    LectureProblemProgress save(LectureProblemProgress lectureProblemProgress);

    Optional<LectureProblemProgress> findByUserIdAndCourseProblemStepId(Long userId, Long courseProblemStepId);

    long countCompletedByUserIdAndCourseProblemStepIds(Long userId, List<Long> courseProblemStepIds);
}
