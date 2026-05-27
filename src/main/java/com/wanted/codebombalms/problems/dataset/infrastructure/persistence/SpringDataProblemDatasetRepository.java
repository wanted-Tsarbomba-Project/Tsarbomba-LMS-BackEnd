package com.wanted.codebombalms.problems.dataset.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataProblemDatasetRepository extends JpaRepository<ProblemDatasetJpaEntity, Long> {

    @Query(
            value = """
                    SELECT pd.*
                    FROM problem_dataset pd
                    JOIN problem p ON p.problem_set_id = pd.problem_set_id
                    WHERE p.problem_id = :problemId
                      AND pd.status = :status
                    ORDER BY pd.dataset_id DESC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<ProblemDatasetJpaEntity> findFirstByProblem_ProblemIdAndStatus(
            @Param("problemId") Long problemId,
            @Param("status") String status
    );

    Optional<ProblemDatasetJpaEntity> findFirstByProblemSet_ProblemSetIdAndStatus(Long problemSetId, String status);

    Optional<ProblemDatasetJpaEntity> findFirstByProblemSet_ProblemSetIdAndStatusOrderByDatasetIdDesc(
            Long problemSetId,
            String status
    );
}