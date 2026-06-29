package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.infrastructure.cache.CacheNames;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LearningProgressCountCacheService {

    private final LectureProgressRepository lectureProgressRepository;
    private final LectureProblemProgressRepository lectureProblemProgressRepository;

    @Cacheable(
            cacheNames = CacheNames.LEARNING_COMPLETED_LECTURE_COUNTS,
            key = "T(com.wanted.codebombalms.learning.infrastructure.cache.LearningCacheKeys)"
                    + ".progressCountPage(#courseId, #studentIds)",
            condition = "#studentIds != null && !#studentIds.isEmpty()",
            unless = "#result == null"
    )
    public Map<Long, Long> countCompletedLectures(Long courseId, List<Long> studentIds, List<Long> lectureIds) {
        return lectureProgressRepository.countCompletedByUserIdsAndLectureIds(studentIds, lectureIds);
    }

    @Cacheable(
            cacheNames = CacheNames.LEARNING_COMPLETED_PROBLEM_COUNTS,
            key = "T(com.wanted.codebombalms.learning.infrastructure.cache.LearningCacheKeys)"
                    + ".progressCountPage(#courseId, #studentIds)",
            condition = "#studentIds != null && !#studentIds.isEmpty()",
            unless = "#result == null"
    )
    public Map<Long, Long> countCompletedProblems(
            Long courseId,
            List<Long> studentIds,
            List<Long> lectureProblemSetIds
    ) {
        return lectureProblemProgressRepository.countCompletedByUserIdsAndLectureProblemSetIds(
                studentIds,
                lectureProblemSetIds
        );
    }
}
