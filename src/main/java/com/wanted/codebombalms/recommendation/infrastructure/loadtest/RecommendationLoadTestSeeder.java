package com.wanted.codebombalms.recommendation.infrastructure.loadtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Recommendation 목록 조회 baseline 용 부하테스트 시드.
 *
 * <p>{@code loadtest} 프로파일에서만 실행한다. 추천 목록 query 는 로그인 사용자 기준
 * {@code problem_recommendation -> problem_set -> problem_category -> problem_progress} 를 조인하므로,
 * 빈 loadtest DB 에서도 같은 query 경로를 타도록 추천 후보와 완료 progress 일부를 만든다.
 */
@Slf4j
@Component
@Profile("loadtest")
@DependsOn("chatListLoadTestSeeder")
@RequiredArgsConstructor
public class RecommendationLoadTestSeeder implements ApplicationRunner {

    private static final String LOGIN_EMAIL = "u01@test.com";
    private static final String LOADTEST_PASSWORD = "Test1234!";
    private static final int RECOMMENDATION_SETS = 200;
    private static final int COMPLETED_EVERY = 10;
    private static final int BATCH_USERS = 120;
    private static final int BATCH_COMPLETED_PER_USER = 12;
    private static final int BATCH_EXISTING_RECOMMENDATIONS_PER_USER = 3;

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Long already = jdbc.queryForObject("select count(*) from problem_recommendation", Long.class);
        if (already != null && already > 0) {
            log.info("event=loadtest_recommendation_seed_skipped reason=already_seeded recommendations={}", already);
            return;
        }

        long startedAt = System.nanoTime();

        Long userId = findUserId(LOGIN_EMAIL);
        Long categoryId = seedCategory();
        List<Long> problemSetIds = seedProblemSets(userId, categoryId);
        seedRecommendations(userId, problemSetIds);
        seedCompletedProgress(userId, problemSetIds);
        List<Long> batchUserIds = seedBatchUsers();
        seedBatchCompletedProgress(batchUserIds, problemSetIds);
        seedBatchExistingRecommendations(batchUserIds, problemSetIds);

        log.info("event=loadtest_recommendation_seed_completed userId={} batchUsers={} problemSets={} recommendations={} durationMs={}",
                userId, batchUserIds.size(), problemSetIds.size(),
                RECOMMENDATION_SETS + batchUserIds.size() * BATCH_EXISTING_RECOMMENDATIONS_PER_USER,
                (System.nanoTime() - startedAt) / 1_000_000);
    }

    private Long findUserId(String email) {
        return jdbc.queryForObject("select user_id from users where email = ?", Long.class, email);
    }

    private Long seedCategory() {
        jdbc.update("""
                insert into problem_category (category_name, description, status)
                values ('추천 부하 카테고리', 'loadtest recommendation category', 'ACTIVE')
                """);
        return jdbc.queryForObject("""
                select category_id
                from problem_category
                where category_name = '추천 부하 카테고리'
                """, Long.class);
    }

    private List<Long> seedProblemSets(Long userId, Long categoryId) {
        jdbc.batchUpdate("""
                insert into problem_set
                  (category_id, title, description, difficulty, status,
                   total_problem_count, completed_user_count, started_user_count, created_by, created_at)
                values (?, ?, ?, 'MEDIUM', 'ACTIVE', 1, 0, 0, ?, now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, categoryId);
                ps.setString(2, "추천 부하 문제세트 " + (i + 1));
                ps.setString(3, "recommendation loadtest problem set " + (i + 1));
                ps.setLong(4, userId);
            }

            @Override
            public int getBatchSize() {
                return RECOMMENDATION_SETS;
            }
        });

        return jdbc.queryForList("""
                select problem_set_id
                from problem_set
                where title like '추천 부하 문제세트 %'
                order by problem_set_id
                """, Long.class);
    }

    private void seedRecommendations(Long userId, List<Long> problemSetIds) {
        jdbc.batchUpdate("""
                insert into problem_recommendation
                  (user_id, problem_set_id, support, confidence, lift, rank_no, status, algorithm, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, 'ACTIVE', 'APRIORI', now(6), now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, userId);
                ps.setLong(2, problemSetIds.get(i));
                ps.setBigDecimal(3, BigDecimal.valueOf(0.010000 + (i % 10) * 0.001000));
                ps.setBigDecimal(4, BigDecimal.valueOf(0.500000 + (i % 20) * 0.010000));
                ps.setBigDecimal(5, BigDecimal.valueOf(1.000000 + (i % 30) * 0.010000));
                ps.setInt(6, i + 1);
            }

            @Override
            public int getBatchSize() {
                return problemSetIds.size();
            }
        });
    }

    private void seedCompletedProgress(Long userId, List<Long> problemSetIds) {
        List<Long> completedProblemSetIds = problemSetIds.stream()
                .filter(problemSetId -> problemSetIds.indexOf(problemSetId) % COMPLETED_EVERY == COMPLETED_EVERY - 1)
                .toList();

        jdbc.batchUpdate("""
                insert into problem_progress
                  (user_id, problem_set_id, current_problem_number, is_completed, completed_at, updated_at)
                values (?, ?, 1, true, now(6), now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, userId);
                ps.setLong(2, completedProblemSetIds.get(i));
            }

            @Override
            public int getBatchSize() {
                return completedProblemSetIds.size();
            }
        });
    }

    private List<Long> seedBatchUsers() {
        jdbc.batchUpdate("""
                insert into users
                  (role, email, password, name, nickname, provider, email_verified, is_locked, created_at, updated_at)
                values
                  ('STUDENT', ?, ?, ?, ?, 'LOCAL', true, false, date_sub(now(6), interval ? day), now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int no = i + 1;
                ps.setString(1, "recommendation-batch-" + no + "@test.com");
                ps.setString(2, passwordEncoder.encode(LOADTEST_PASSWORD));
                ps.setString(3, "추천배치대상 " + no);
                ps.setString(4, "recommendation-batch-" + no);
                ps.setInt(5, 10 + (i % 30));
            }

            @Override
            public int getBatchSize() {
                return BATCH_USERS;
            }
        });

        return jdbc.queryForList("""
                select user_id
                from users
                where email like 'recommendation-batch-%@test.com'
                order by user_id
                """, Long.class);
    }

    private void seedBatchCompletedProgress(List<Long> batchUserIds, List<Long> problemSetIds) {
        int progressCount = batchUserIds.size() * BATCH_COMPLETED_PER_USER;
        jdbc.batchUpdate("""
                insert into problem_progress
                  (user_id, problem_set_id, current_problem_number, is_completed, completed_at, updated_at)
                values (?, ?, 1, true, date_sub(now(6), interval ? day), now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int userIndex = i / BATCH_COMPLETED_PER_USER;
                int setIndex = (userIndex * 7 + i) % problemSetIds.size();
                ps.setLong(1, batchUserIds.get(userIndex));
                ps.setLong(2, problemSetIds.get(setIndex));
                ps.setInt(3, 1 + (i % 20));
            }

            @Override
            public int getBatchSize() {
                return progressCount;
            }
        });
    }

    private void seedBatchExistingRecommendations(List<Long> batchUserIds, List<Long> problemSetIds) {
        int recommendationCount = batchUserIds.size() * BATCH_EXISTING_RECOMMENDATIONS_PER_USER;
        jdbc.batchUpdate("""
                insert into problem_recommendation
                  (user_id, problem_set_id, support, confidence, lift, rank_no, status, algorithm, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, 'ACTIVE', 'APRIORI', date_sub(now(6), interval 1 day), now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int userIndex = i / BATCH_EXISTING_RECOMMENDATIONS_PER_USER;
                int rankNo = i % BATCH_EXISTING_RECOMMENDATIONS_PER_USER + 1;
                int setIndex = (userIndex * 11 + rankNo) % problemSetIds.size();

                ps.setLong(1, batchUserIds.get(userIndex));
                ps.setLong(2, problemSetIds.get(setIndex));
                ps.setBigDecimal(3, BigDecimal.valueOf(0.020000 + rankNo * 0.001000));
                ps.setBigDecimal(4, BigDecimal.valueOf(0.600000 + rankNo * 0.010000));
                ps.setBigDecimal(5, BigDecimal.valueOf(1.200000 + rankNo * 0.010000));
                ps.setInt(6, rankNo);
            }

            @Override
            public int getBatchSize() {
                return recommendationCount;
            }
        });
    }
}
