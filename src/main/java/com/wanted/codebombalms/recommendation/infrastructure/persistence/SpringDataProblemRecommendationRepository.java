package com.wanted.codebombalms.recommendation.infrastructure.persistence;

import com.wanted.codebombalms.recommendation.domain.model.RecommendationStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** problem_recommendation 조회용 Spring Data JPA repository입니다. */
public interface SpringDataProblemRecommendationRepository
        extends JpaRepository<ProblemRecommendationJpaEntity, Long> {

    /** 활성 추천과 활성 문제 세트를 조인해 완료된 문제 세트를 제외하고 조회합니다. */
    @Query(
            value = """
                    SELECT
                        pr.recommendation_id AS recommendationId,
                        pr.problem_set_id AS problemSetId,
                        ps.title AS title,
                        ps.description AS description,
                        ps.difficulty AS difficulty,
                        CASE
                            WHEN ps.started_user_count = 0 THEN 0.0
                            ELSE ROUND(ps.completed_user_count * 100.0 / ps.started_user_count, 1)
                        END AS accuracyRate,
                        pc.category_id AS categoryId,
                        pc.category_name AS categoryName,
                        pr.rank_no AS rankNo
                    FROM problem_recommendation pr
                    JOIN problem_set ps ON ps.problem_set_id = pr.problem_set_id
                    JOIN problem_category pc ON pc.category_id = ps.category_id
                    LEFT JOIN problem_progress pp
                        ON pp.user_id = pr.user_id
                        AND pp.problem_set_id = pr.problem_set_id
                        AND pp.is_completed = true
                    WHERE pr.user_id = :userId
                        AND pr.status = 'ACTIVE'
                        AND ps.status = 'ACTIVE'
                        AND pc.status = 'ACTIVE'
                        AND pp.progress_id IS NULL
                    ORDER BY pr.rank_no ASC, pr.recommendation_id ASC
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<ProblemSetRecommendationProjection> findActiveProblemSetRecommendations(
            @Param("userId") Long userId,
            @Param("limit") int limit
    );

    /** 사용자별 활성 추천 row를 잠가 같은 트랜잭션 안의 교체 저장 경합을 줄입니다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT pr
            FROM ProblemRecommendationJpaEntity pr
            WHERE pr.userId = :userId
                AND pr.status = :status
            """)
    List<ProblemRecommendationJpaEntity> lockByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") RecommendationStatus status
    );

    /** 지정 사용자에게 기존에 노출 중이던 추천 row를 모두 비활성화합니다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    UPDATE problem_recommendation
                    SET status = 'INACTIVE',
                        updated_at = :updatedAt
                    WHERE user_id = :userId
                        AND status = 'ACTIVE'
                    """,
            nativeQuery = true
    )
    int deactivateActiveByUserId(
            @Param("userId") Long userId,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    /** 비활성화된 지 오래된 추천 row를 하드 딜리트합니다. */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    DELETE FROM problem_recommendation
                    WHERE status = 'INACTIVE'
                        AND updated_at < :threshold
                    """,
            nativeQuery = true
    )
    int hardDeleteInactiveByUpdatedAtBefore(@Param("threshold") LocalDateTime threshold);

    /** native query 결과를 추천 응답 구성에 필요한 필드로 투영합니다. */
    interface ProblemSetRecommendationProjection {

        Long getRecommendationId();

        Long getProblemSetId();

        String getTitle();

        String getDescription();

        String getDifficulty();

        Double getAccuracyRate();

        Long getCategoryId();

        String getCategoryName();

        Integer getRankNo();
    }
}
