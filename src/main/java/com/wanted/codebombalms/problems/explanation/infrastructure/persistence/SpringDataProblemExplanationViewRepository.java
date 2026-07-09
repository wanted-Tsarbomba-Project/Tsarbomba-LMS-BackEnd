package com.wanted.codebombalms.problems.explanation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SpringDataProblemExplanationViewRepository
        extends JpaRepository<ProblemExplanationViewJpaEntity, Long> {

    boolean existsByUserIdAndProblemId(Long userId, Long problemId);

    List<ProblemExplanationViewJpaEntity> findByUserIdAndProblemIdIn(
            Long userId,
            Collection<Long> problemIds
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            insert into problem_explanation_view
                (user_id, problem_id, problem_set_id, viewed_at)
            values
                (:userId, :problemId, :problemSetId, current_timestamp(6))
            on duplicate key update viewed_at = viewed_at
            """, nativeQuery = true)
    int saveViewedIgnoreDuplicate(
            @Param("userId") Long userId,
            @Param("problemId") Long problemId,
            @Param("problemSetId") Long problemSetId
    );
}
