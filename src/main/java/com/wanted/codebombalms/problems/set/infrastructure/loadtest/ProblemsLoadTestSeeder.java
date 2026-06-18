package com.wanted.codebombalms.problems.set.infrastructure.loadtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Problems 도메인 baseline 부하 테스트용 시드 데이터.
 *
 * <p>{@code loadtest} 프로파일에서만 실행한다. 운영 DB에는 절대 사용하지 않는다.
 */
@Slf4j
@Component
@Profile("loadtest")
@RequiredArgsConstructor
public class ProblemsLoadTestSeeder implements ApplicationRunner {

    private static final String LOGIN_EMAIL = "u01@test.com";
    private static final String LOGIN_PASSWORD = "Test1234!";
    private static final long CATEGORY_ID = 3001L;
    private static final long PROBLEM_SET_ID = 4001L;
    private static final long FIRST_PROBLEM_ID = 5001L;
    private static final int PROBLEM_COUNT = 20;

    private static final String DATASET_ORIGINAL_FILE_NAME = "employee_performance.csv";
    private static final String DATASET_OBJECT_NAME = "problem_dataset/"
            + "7ecb57b8-51ff-48ee-ad72-63e81cea8d6d_"
            + "28679fde-6075-4c2e-a3f0-190fa3d80db7_employee_performance.csv.csv";
    private static final String DATASET_URL = "https://storage.googleapis.com/codebombalms/"
            + DATASET_OBJECT_NAME;

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Long alreadySeeded = jdbc.queryForObject(
                "select count(*) from problem_set where problem_set_id = ?",
                Long.class,
                PROBLEM_SET_ID
        );

        if (alreadySeeded != null && alreadySeeded > 0) {
            log.info(
                    "event=problems_loadtest_seed_skipped reason=already_seeded problemSetId={}",
                    PROBLEM_SET_ID
            );
            return;
        }

        long startNanos = System.nanoTime();

        Long userId = findOrCreateLoginUser();
        seedCategory();
        seedProblemSet(userId);
        seedProblems();
        seedDataset();
        seedTestCases();
        seedProgress(userId);

        log.info(
                "event=problems_loadtest_seed_completed userId={} problemSetId={} problems={} testCases={} durationMs={}",
                userId,
                PROBLEM_SET_ID,
                PROBLEM_COUNT,
                PROBLEM_COUNT,
                elapsedMillis(startNanos)
        );
    }

    private Long findOrCreateLoginUser() {
        Long userId = jdbc.query(
                "select user_id from users where email = ? limit 1",
                rs -> rs.next() ? rs.getLong("user_id") : null,
                LOGIN_EMAIL
        );

        if (userId != null) {
            return userId;
        }

        String hash = passwordEncoder.encode(LOGIN_PASSWORD);
        jdbc.update("""
                insert into users
                  (role, email, password, name, nickname, provider, email_verified, is_locked, created_at, updated_at)
                values
                  ('STUDENT', ?, ?, 'Loadtest User', 'loadtester-problems', 'LOCAL', true, false, now(6), now(6))
                """, LOGIN_EMAIL, hash);

        return jdbc.queryForObject(
                "select user_id from users where email = ?",
                Long.class,
                LOGIN_EMAIL
        );
    }

    private void seedCategory() {
        jdbc.update("""
                insert ignore into problem_category
                  (category_id, category_name, description, status)
                values
                  (?, 'Loadtest Python Data Analysis', 'Problems loadtest category', 'ACTIVE')
                """, CATEGORY_ID);
    }

    private void seedProblemSet(Long userId) {
        jdbc.update("""
                insert into problem_set
                  (problem_set_id, category_id, title, description, difficulty, status,
                   total_problem_count, completed_user_count, started_user_count, created_by, created_at)
                values
                  (?, ?, 'Problems Loadtest Problem Set',
                   'Problem set entry and submission baseline data.',
                   'MEDIUM', 'ACTIVE', ?, 0, 0, ?, now(6))
                """, PROBLEM_SET_ID, CATEGORY_ID, PROBLEM_COUNT, userId);
    }

    private void seedProblems() {
        jdbc.batchUpdate("""
                insert into problem
                  (problem_id, problem_set_id, title, content, problem_type, difficulty,
                   explanation, point, attempt_limit, is_retriable, status, problem_order)
                values
                  (?, ?, ?, ?, 'CODE', 'EASY', ?, 10, 999, true, 'ACTIVE', ?)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                long problemId = FIRST_PROBLEM_ID + i;
                int order = i + 1;

                ps.setLong(1, problemId);
                ps.setLong(2, PROBLEM_SET_ID);
                ps.setString(3, "Loadtest Problem " + order);
                ps.setString(4, "Load employee_performance.csv and assign len(df) to result.");
                ps.setString(5, "Use len(df) to count rows.");
                ps.setInt(6, order);
            }

            @Override
            public int getBatchSize() {
                return PROBLEM_COUNT;
            }
        });
    }

    private void seedDataset() {
        jdbc.update("""
                insert into problem_dataset
                  (problem_set_id, original_file_name, stored_file_name, file_url, file_path,
                   file_size, meta_data, status, created_at, updated_at)
                values
                  (?, ?, ?, ?, ?, 20480, '{}', 'ACTIVE', now(6), now(6))
                """,
                PROBLEM_SET_ID,
                DATASET_ORIGINAL_FILE_NAME,
                DATASET_OBJECT_NAME.substring(DATASET_OBJECT_NAME.lastIndexOf('/') + 1),
                DATASET_URL,
                DATASET_OBJECT_NAME
        );
    }

    private void seedTestCases() {
        jdbc.batchUpdate("""
                insert into problem_test_case
                  (problem_id, test_code, test_order, is_hidden, timeout_ms, status, created_at, updated_at)
                values
                  (?, 'assert result == len(df)', 1, false, 3000, 'ACTIVE', now(6), now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, FIRST_PROBLEM_ID + i);
            }

            @Override
            public int getBatchSize() {
                return PROBLEM_COUNT;
            }
        });
    }

    private void seedProgress(Long userId) {
        jdbc.update("""
                insert into problem_progress
                  (user_id, problem_set_id, current_problem_number, is_completed, completed_at, updated_at)
                values
                  (?, ?, 1, false, null, now(6))
                """, userId, PROBLEM_SET_ID);
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
