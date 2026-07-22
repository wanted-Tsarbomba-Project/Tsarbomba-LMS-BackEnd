package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 추천 질문 생성의 '원천' 조회 — chat_room ⨝ chat_message(role=USER)를 문제 단위로 집계.
 * ChatMessageJpaEntity 를 기준 엔티티로만 쓰고 실제 조회는 네이티브 쿼리다.
 */
public interface SuggestedQuestionSourceJpaRepository extends JpaRepository<ChatMessageJpaEntity, Long> {

    /** 유저수·질문수 임계값을 넘는 (문제세트, 문제) 쌍만 반환. */
    interface EligibleProblem {
        Long getProblemSetId();
        Long getProblemId();
    }

    @Query(value = """
            SELECT cr.problem_set_id AS problemSetId,
                   cr.problem_id     AS problemId
            FROM chat_room cr
            JOIN chat_message cm ON cm.room_id = cr.room_id
            WHERE cm.role = 'USER'
              AND cr.problem_set_id IS NOT NULL
              AND cr.problem_id IS NOT NULL
            GROUP BY cr.problem_set_id, cr.problem_id
            HAVING COUNT(DISTINCT cr.user_id) >= :minUsers
               AND COUNT(*) >= :minQuestions
            """, nativeQuery = true)
    List<EligibleProblem> findEligibleProblems(@Param("minUsers") int minUsers,
                                               @Param("minQuestions") int minQuestions);

    /** 한 문제의 최신 USER 질문 본문을 최대 limit개. */
    @Query(value = """
            SELECT cm.content
            FROM chat_room cr
            JOIN chat_message cm ON cm.room_id = cr.room_id
            WHERE cr.problem_set_id = :problemSetId
              AND cr.problem_id = :problemId
              AND cm.role = 'USER'
            ORDER BY cm.created_at DESC, cm.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<String> findRecentUserQuestions(@Param("problemSetId") Long problemSetId,
                                         @Param("problemId") Long problemId,
                                         @Param("limit") int limit);
}
