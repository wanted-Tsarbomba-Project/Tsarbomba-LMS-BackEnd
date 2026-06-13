package com.wanted.codebombalms.recommendation.infrastructure.persistence;

import com.wanted.codebombalms.recommendation.domain.model.RecommendationHide;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationHideType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** recommendation_hide 테이블과 매핑되는 추천 숨김 엔티티입니다. */
@Entity
@Table(name = "recommendation_hide")
public class RecommendationHideJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hide_id")
    private Long hideId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "hide_type", nullable = false, length = 50)
    private RecommendationHideType hideType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "hidden_until")
    private LocalDateTime hiddenUntil;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** JPA가 엔티티를 생성할 때 사용하는 기본 생성자입니다. */
    protected RecommendationHideJpaEntity() {
    }

    /** 전체 문제 세트 추천 숨김 row를 새로 생성합니다. */
    public RecommendationHideJpaEntity(Long userId, RecommendationHideType hideType, LocalDateTime hiddenUntil) {
        LocalDateTime now = LocalDateTime.now();
        this.userId = userId;
        this.hideType = hideType;
        this.targetId = null;
        this.hiddenUntil = hiddenUntil;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** 숨김 만료 시각과 수정 시각을 갱신합니다. */
    public void updateHiddenUntil(LocalDateTime hiddenUntil) {
        this.hiddenUntil = hiddenUntil;
        this.updatedAt = LocalDateTime.now();
    }

    /** persistence 엔티티를 application/domain에서 쓰는 모델로 변환합니다. */
    public RecommendationHide toDomain() {
        return new RecommendationHide(
                hideId,
                userId,
                hideType,
                targetId,
                hiddenUntil,
                createdAt,
                updatedAt
        );
    }
}
