package com.wanted.codebombalms.learning.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLectureProblemSubmissionRepository
        extends JpaRepository<LectureProblemSubmissionJpaEntity, Long> {

    int countByUserIdAndCourseProblemStepId(Long userId, Long courseProblemStepId);
}
