package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** problem_suggested_question 테이블 매핑. 문제 단위 공유라 user_id 없음, 재생성 시 통째 교체. */
@Entity
@Table(name = "problem_suggested_question")
@Getter
@NoArgsConstructor
public class SuggestedQuestionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "problem_set_id", nullable = false)
    private Long problemSetId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private SuggestedQuestionJpaEntity(Long problemSetId, Long problemId, String question, Integer rankNo) {
        this.problemSetId = problemSetId;
        this.problemId = problemId;
        this.question = question;
        this.rankNo = rankNo;
    }

    /** 생성 배치 결과 1건을 새 저장 엔티티로 만든다. */
    public static SuggestedQuestionJpaEntity of(Long problemSetId, Long problemId, String question, int rankNo) {
        return new SuggestedQuestionJpaEntity(problemSetId, problemId, question, rankNo);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
