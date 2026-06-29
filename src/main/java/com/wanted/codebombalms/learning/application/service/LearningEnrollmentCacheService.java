package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.infrastructure.cache.CacheNames;
import com.wanted.codebombalms.learning.application.port.LearningEnrollmentPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LearningEnrollmentCacheService {

    private final LearningEnrollmentPort learningEnrollmentPort;

    @Cacheable(
            cacheNames = CacheNames.LEARNING_ACTIVE_STUDENT_COUNT,
            key = "T(com.wanted.codebombalms.learning.infrastructure.cache.LearningCacheKeys)"
                    + ".activeStudentCount(#courseId)",
            condition = "#courseId != null"
    )
    public long countActiveStudentsByCourse(Long courseId) {
        return learningEnrollmentPort.countActiveStudentsByCourse(courseId);
    }

    @Cacheable(
            cacheNames = CacheNames.LEARNING_STUDENT_ID_PAGE,
            key = "T(com.wanted.codebombalms.learning.infrastructure.cache.LearningCacheKeys)"
                    + ".studentIdPage(#courseId, #page, #size)",
            condition = "#courseId != null && #page >= 0 && #size > 0",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<Long> findActiveStudentIdsByCourse(Long courseId, int page, int size) {
        return learningEnrollmentPort.findActiveStudentIdsByCourse(courseId, page, size);
    }
}
