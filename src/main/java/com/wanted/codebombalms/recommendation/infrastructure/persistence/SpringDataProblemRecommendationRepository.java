package com.wanted.codebombalms.recommendation.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** problem_recommendation 조회용 Spring Data JPA repository입니다. */
public interface SpringDataProblemRecommendationRepository
        extends JpaRepository<ProblemRecommendationJpaEntity, Long> {

    /** 활성 추천과 활성 문제 세트를 조인해 완료된 문제 세트를 제외하고 조회합니다. */
    @Query(
            value = """
                    SELECT
                        pr.recommendation_id AS recommendationId,
                        pr.problem_set_id AS problemSetId,
                        ps.category_id AS categoryId,
                        ps.created_by AS creatorId,
                        pr.support AS support,
                        pr.confidence AS confidence,
                        pr.lift AS lift,
                        pr.rank_no AS rankNo,
                        pr.algorithm AS algorithm,
                        pr.created_at AS createdAt
                    FROM problem_recommendation pr
                    JOIN problem_set ps ON ps.problem_set_id = pr.problem_set_id
                    LEFT JOIN problem_progress pp
                        ON pp.user_id = pr.user_id
                        AND pp.problem_set_id = pr.problem_set_id
                        AND pp.is_completed = true
                    WHERE pr.user_id = :userId
                        AND pr.status = 'ACTIVE'
                        AND ps.status = 'ACTIVE'
                        AND pp.progress_id IS NULL
                    ORDER BY pr.rank_no ASC
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<ProblemSetRecommendationProjection> findActiveProblemSetRecommendations(
            @Param("userId") Long userId,
            @Param("limit") int limit
    );

    /** native query 결과를 추천 응답 구성에 필요한 필드로 투영합니다. */
    interface ProblemSetRecommendationProjection {

        Long getRecommendationId();

        Long getProblemSetId();

        Long getCategoryId();

        Long getCreatorId();

        BigDecimal getSupport();

        BigDecimal getConfidence();

        BigDecimal getLift();

        Integer getRankNo();

        String getAlgorithm();

        LocalDateTime getCreatedAt();
    }
}
