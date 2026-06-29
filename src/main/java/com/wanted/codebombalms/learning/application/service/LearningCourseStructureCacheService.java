package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.infrastructure.cache.CacheNames;
import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import com.wanted.codebombalms.learning.application.port.LearningLecturePort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LearningCourseStructureCacheService {

    private final LearningLecturePort learningLecturePort;
    private final LearningCourseProblemPort learningCourseProblemPort;

    @Cacheable(
            cacheNames = CacheNames.LEARNING_COURSE_LECTURE_IDS,
            key = "T(com.wanted.codebombalms.learning.infrastructure.cache.LearningCacheKeys).courseStructure(#courseId)",
            condition = "#courseId != null",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<Long> findLectureIdsByCourse(Long courseId) {
        return learningLecturePort.findLectureIdsByCourse(courseId);
    }

    @Cacheable(
            cacheNames = CacheNames.LEARNING_COURSE_PROBLEM_SET_IDS,
            key = "T(com.wanted.codebombalms.learning.infrastructure.cache.LearningCacheKeys).courseStructure(#courseId)",
            condition = "#courseId != null",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<Long> findMainLectureProblemSetIdsByCourse(Long courseId) {
        return learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(courseId);
    }
}
