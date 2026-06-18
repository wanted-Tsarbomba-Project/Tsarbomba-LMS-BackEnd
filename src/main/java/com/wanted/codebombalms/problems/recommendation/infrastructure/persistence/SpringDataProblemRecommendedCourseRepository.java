package com.wanted.codebombalms.problems.recommendation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataProblemRecommendedCourseRepository
        extends JpaRepository<ProblemRecommendedCourseJpaEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from ProblemRecommendedCourseJpaEntity recommendation
            where recommendation.problemId = :problemId
            """)
    void deleteByProblemId(@Param("problemId") Long problemId);

    List<ProblemRecommendedCourseJpaEntity> findByProblemIdAndStatusOrderByDisplayOrderAsc(
            Long problemId,
            String status
    );
}
