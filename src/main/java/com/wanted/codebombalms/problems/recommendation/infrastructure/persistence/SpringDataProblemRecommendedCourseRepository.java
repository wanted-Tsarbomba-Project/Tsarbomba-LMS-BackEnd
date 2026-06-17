package com.wanted.codebombalms.problems.recommendation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataProblemRecommendedCourseRepository
        extends JpaRepository<ProblemRecommendedCourseJpaEntity, Long> {

    void deleteByProblemId(Long problemId);

    List<ProblemRecommendedCourseJpaEntity> findByProblemIdAndStatusOrderByDisplayOrderAsc(
            Long problemId,
            String status
    );
}
