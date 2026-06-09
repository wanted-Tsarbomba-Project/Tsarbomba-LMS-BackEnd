package com.wanted.codebombalms.problems.problem.infrastructure.persistence;

import com.wanted.codebombalms.problems.problem.application.port.ProblemTargetDetailPort.ProblemTargetDetailView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataProblemRepository extends JpaRepository<ProblemJpaEntity, Long> {

    List<ProblemJpaEntity> findByProblemSet_Category_CategoryIdAndStatusOrderByProblemOrderAsc(
            Long categoryId,
            String status
    );

    Optional<ProblemJpaEntity> findByProblemSet_ProblemSetIdAndProblemOrderAndStatus(
            Long problemSetId,
            Integer problemOrder,
            String status
    );

    List<ProblemJpaEntity> findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(
            Long problemSetId,
            String status
    );

    Optional<ProblemJpaEntity> findTopByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderDesc(
            Long problemSetId,
            String status
    );

    Optional<ProblemJpaEntity> findByProblemIdAndProblemSet_ProblemSetId(
            Long problemId,
            Long problemSetId
    );

    Optional<ProblemJpaEntity> findByProblemIdAndStatus(Long problemId, String status);

    @Query("""
            select new com.wanted.codebombalms.problems.problem.application.port.ProblemTargetDetailPort$ProblemTargetDetailView(
                p.problemId,
                p.title,
                p.status,
                ps.problemSetId,
                ps.title,
                ps.createdBy
            )
            from ProblemJpaEntity p
            join p.problemSet ps
            where p.problemId = :problemId
            """)
    Optional<ProblemTargetDetailView> findProblemTargetDetail(@Param("problemId") Long problemId);
}
