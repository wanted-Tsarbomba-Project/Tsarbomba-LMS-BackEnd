package com.wanted.codebombalms.recommendation.infrastructure.persistence;

import com.wanted.codebombalms.recommendation.application.command.GeneratedProblemSetRecommendation;
import com.wanted.codebombalms.recommendation.application.command.GeneratedUserProblemSetRecommendations;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationAlgorithm;
import com.wanted.codebombalms.recommendation.infrastructure.metrics.RecommendationMetrics;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** 추천 생성 결과가 problem_recommendation 테이블에 저장되는 형태를 검증합니다. */
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(ProblemRecommendationCommandAdapter.class)
class ProblemRecommendationCommandAdapterTest {

    @Autowired
    private ProblemRecommendationCommandAdapter adapter;

    @Autowired
    private SpringDataProblemRecommendationRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private RecommendationMetrics recommendationMetrics;

    /** Python 반환값 3개가 dev DB에서 보이는 컬럼 형태 그대로 ACTIVE row로 저장됩니다. */
    @Test
    @DisplayName("추천 생성 결과 3개를 ACTIVE 상태로 insert하고 테이블에서 조회할 수 있다.")
    void replaceActiveRecommendations_insertsThreeActiveRows() {
        Long userId = 10L;

        adapter.replaceActiveRecommendations(new GeneratedUserProblemSetRecommendations(
                userId,
                List.of(
                        generatedRecommendation(2001L, 1),
                        generatedRecommendation(2002L, 2),
                        generatedRecommendation(2003L, 3)
                )
        ));
        repository.flush();

        List<Map<String, Object>> rows = findRowsByUserId(userId);

        assertEquals(3, rows.size());
        assertRecommendationRow(rows.get(0), userId, 2001L, "ACTIVE", 1);
        assertRecommendationRow(rows.get(1), userId, 2002L, "ACTIVE", 2);
        assertRecommendationRow(rows.get(2), userId, 2003L, "ACTIVE", 3);
    }

    /** 기존 ACTIVE 추천이 있는 사용자는 신규 추천 저장 전 기존 row가 INACTIVE로 전환됩니다. */
    @Test
    @DisplayName("기존 ACTIVE 추천이 있으면 INACTIVE 처리 후 신규 추천 3개를 ACTIVE로 insert한다.")
    void replaceActiveRecommendations_deactivatesExistingActiveRowsBeforeInsert() {
        Long userId = 10L;
        Long otherUserId = 20L;
        LocalDateTime now = LocalDateTime.of(2026, 6, 15, 5, 0);

        repository.saveAll(List.of(
                ProblemRecommendationJpaEntity.active(
                        userId,
                        1001L,
                        BigDecimal.valueOf(0.01),
                        BigDecimal.valueOf(0.40),
                        BigDecimal.valueOf(1.10),
                        1,
                        RecommendationAlgorithm.APRIORI,
                        now.minusDays(1)
                ),
                ProblemRecommendationJpaEntity.active(
                        userId,
                        1002L,
                        BigDecimal.valueOf(0.02),
                        BigDecimal.valueOf(0.50),
                        BigDecimal.valueOf(1.20),
                        2,
                        RecommendationAlgorithm.APRIORI,
                        now.minusDays(1)
                ),
                ProblemRecommendationJpaEntity.active(
                        otherUserId,
                        9001L,
                        BigDecimal.valueOf(0.09),
                        BigDecimal.valueOf(0.90),
                        BigDecimal.valueOf(1.90),
                        1,
                        RecommendationAlgorithm.APRIORI,
                        now.minusDays(1)
                )
        ));
        repository.flush();

        adapter.replaceActiveRecommendations(new GeneratedUserProblemSetRecommendations(
                userId,
                List.of(
                        generatedRecommendation(2001L, 1),
                        generatedRecommendation(2002L, 2),
                        generatedRecommendation(2003L, 3)
                )
        ));
        repository.flush();

        List<Map<String, Object>> userRows = findRowsByUserId(userId);
        List<Map<String, Object>> otherUserRows = findRowsByUserId(otherUserId);

        assertEquals(5, userRows.size());
        assertEquals("INACTIVE", statusByProblemSetId(userRows, 1001L));
        assertEquals("INACTIVE", statusByProblemSetId(userRows, 1002L));
        assertEquals("ACTIVE", statusByProblemSetId(userRows, 2001L));
        assertEquals("ACTIVE", statusByProblemSetId(userRows, 2002L));
        assertEquals("ACTIVE", statusByProblemSetId(userRows, 2003L));

        assertEquals(3, countByStatus(userRows, "ACTIVE"));
        assertEquals(2, countByStatus(userRows, "INACTIVE"));
        assertEquals(1, otherUserRows.size());
        assertRecommendationRow(otherUserRows.get(0), otherUserId, 9001L, "ACTIVE", 1);
    }

    /** INACTIVE 상태이고 기준 시각보다 오래된 추천 row만 하드 딜리트됩니다. */
    @Test
    @DisplayName("updated_at이 기준 시각 이전인 INACTIVE 추천 row만 hard delete한다.")
    void hardDeleteInactiveByUpdatedAtBefore_deletesOldInactiveRowsOnly() {
        Long userId = 10L;
        LocalDateTime threshold = LocalDateTime.of(2026, 6, 15, 3, 0);

        repository.saveAll(List.of(
                ProblemRecommendationJpaEntity.active(
                        userId,
                        1001L,
                        BigDecimal.valueOf(0.01),
                        BigDecimal.valueOf(0.40),
                        BigDecimal.valueOf(1.10),
                        1,
                        RecommendationAlgorithm.APRIORI,
                        threshold.minusMonths(4)
                ),
                ProblemRecommendationJpaEntity.active(
                        userId,
                        1002L,
                        BigDecimal.valueOf(0.02),
                        BigDecimal.valueOf(0.50),
                        BigDecimal.valueOf(1.20),
                        2,
                        RecommendationAlgorithm.APRIORI,
                        threshold.minusMonths(2)
                ),
                ProblemRecommendationJpaEntity.active(
                        userId,
                        1003L,
                        BigDecimal.valueOf(0.03),
                        BigDecimal.valueOf(0.60),
                        BigDecimal.valueOf(1.30),
                        3,
                        RecommendationAlgorithm.APRIORI,
                        threshold.minusMonths(4)
                )
        ));
        repository.flush();

        updateStatusAndUpdatedAt(1001L, "INACTIVE", threshold.minusDays(1));
        updateStatusAndUpdatedAt(1002L, "INACTIVE", threshold.plusDays(1));
        updateStatusAndUpdatedAt(1003L, "ACTIVE", threshold.minusDays(1));

        int deletedCount = repository.hardDeleteInactiveByUpdatedAtBefore(threshold);
        repository.flush();

        List<Map<String, Object>> rows = findRowsByUserId(userId);

        assertEquals(1, deletedCount);
        assertEquals(2, rows.size());
        assertEquals("INACTIVE", statusByProblemSetId(rows, 1002L));
        assertEquals("ACTIVE", statusByProblemSetId(rows, 1003L));
    }

    /** 테스트용 Python 추천 결과 한 건을 생성합니다. */
    private GeneratedProblemSetRecommendation generatedRecommendation(Long problemSetId, int rankNo) {
        return new GeneratedProblemSetRecommendation(
                problemSetId,
                BigDecimal.valueOf(0.03),
                BigDecimal.valueOf(0.70),
                BigDecimal.valueOf(1.50),
                rankNo,
                RecommendationAlgorithm.APRIORI
        );
    }

    /** dev DB 조회 화면과 같은 주요 컬럼 목록으로 추천 row를 조회합니다. */
    private List<Map<String, Object>> findRowsByUserId(Long userId) {
        return jdbcTemplate.queryForList(
                """
                        SELECT
                            recommendation_id,
                            user_id,
                            problem_set_id,
                            support,
                            confidence,
                            lift,
                            rank_no,
                            status,
                            algorithm,
                            created_at,
                            updated_at
                        FROM problem_recommendation
                        WHERE user_id = ?
                        ORDER BY recommendation_id ASC
                        """,
                userId
        );
    }

    /** 테스트 데이터의 상태와 updated_at을 삭제 조건에 맞게 조정합니다. */
    private void updateStatusAndUpdatedAt(Long problemSetId, String status, LocalDateTime updatedAt) {
        jdbcTemplate.update(
                """
                        UPDATE problem_recommendation
                        SET status = ?,
                            updated_at = ?
                        WHERE problem_set_id = ?
                        """,
                status,
                updatedAt,
                problemSetId
        );
    }

    /** 조회된 row의 핵심 저장 컬럼이 기대값과 같은지 검증합니다. */
    private void assertRecommendationRow(
            Map<String, Object> row,
            Long userId,
            Long problemSetId,
            String status,
            int rankNo
    ) {
        assertNotNull(row.get("RECOMMENDATION_ID"));
        assertEquals(userId, ((Number) row.get("USER_ID")).longValue());
        assertEquals(problemSetId, ((Number) row.get("PROBLEM_SET_ID")).longValue());
        assertEquals(status, row.get("STATUS"));
        assertEquals("APRIORI", row.get("ALGORITHM"));
        assertEquals(rankNo, ((Number) row.get("RANK_NO")).intValue());
        assertNotNull(row.get("CREATED_AT"));
        assertNotNull(row.get("UPDATED_AT"));
    }

    /** 문제 세트 ID 기준으로 조회 row의 상태값을 찾습니다. */
    private String statusByProblemSetId(List<Map<String, Object>> rows, Long problemSetId) {
        return rows.stream()
                .filter(row -> ((Number) row.get("PROBLEM_SET_ID")).longValue() == problemSetId)
                .map(row -> (String) row.get("STATUS"))
                .findFirst()
                .orElseThrow();
    }

    /** 조회 row 중 특정 상태값의 개수를 셉니다. */
    private long countByStatus(List<Map<String, Object>> rows, String status) {
        return rows.stream()
                .filter(row -> status.equals(row.get("STATUS")))
                .count();
    }
}
