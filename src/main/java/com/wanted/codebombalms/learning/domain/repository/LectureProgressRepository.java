package com.wanted.codebombalms.learning.domain.repository;

import com.wanted.codebombalms.learning.domain.model.LectureProgress;
import java.util.List;
import java.util.Optional;

public interface LectureProgressRepository {

    LectureProgress save(LectureProgress lectureProgress);

    Optional<LectureProgress> findByUserIdAndLectureId(Long userId, Long lectureId);

    long countCompletedByUserIdAndLectureIds(Long userId, List<Long> lectureIds);
}
