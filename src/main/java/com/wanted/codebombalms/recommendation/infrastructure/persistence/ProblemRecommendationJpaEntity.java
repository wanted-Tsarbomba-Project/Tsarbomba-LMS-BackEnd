package com.wanted.codebombalms.recommendation.infrastructure.persistence;

import com.wanted.codebombalms.recommendation.domain.model.RecommendationAlgorithm;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** problem_recommendation 테이블과 매핑되는 추천 저장 엔티티입니다. */
@Entity
@Table(name = "problem_recommendation")
public class ProblemRecommendationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long recommendationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "problem_set_id", nullable = false)
    private Long problemSetId;

    @Column(name = "support", nullable = false, precision = 10, scale = 6)
    private BigDecimal support;

    @Column(name = "confidence", nullable = false, precision = 10, scale = 6)
    private BigDecimal confidence;

    @Column(name = "lift", nullable = false, precision = 10, scale = 6)
    private BigDecimal lift;

    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RecommendationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm", nullable = false, length = 30)
    private RecommendationAlgorithm algorithm;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** JPA가 엔티티를 생성할 때 사용하는 기본 생성자입니다. */
    protected ProblemRecommendationJpaEntity() {
    }

    /** 추천 생성 결과를 ACTIVE 상태의 신규 저장 엔티티로 만듭니다. */
    public static ProblemRecommendationJpaEntity active(
            Long userId,
            Long problemSetId,
            BigDecimal support,
            BigDecimal confidence,
            BigDecimal lift,
            Integer rankNo,
            RecommendationAlgorithm algorithm,
            LocalDateTime now
    ) {
        ProblemRecommendationJpaEntity entity = new ProblemRecommendationJpaEntity();
        entity.userId = userId;
        entity.problemSetId = problemSetId;
        entity.support = support;
        entity.confidence = confidence;
        entity.lift = lift;
        entity.rankNo = rankNo;
        entity.status = RecommendationStatus.ACTIVE;
        entity.algorithm = algorithm;
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }
}
