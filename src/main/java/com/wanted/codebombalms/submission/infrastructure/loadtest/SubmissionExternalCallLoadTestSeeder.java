package com.wanted.codebombalms.submission.infrastructure.loadtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@Component
@Profile("loadtest-submission")
public class SubmissionExternalCallLoadTestSeeder implements ApplicationRunner {

    private static final int USER_COUNT = 300;
    private static final int TEST_CASE_COUNT = 5;

    private static final long USER_ID_START = 840001L;
    private static final long CATEGORY_ID = 840001L;
    private static final long PROBLEM_SET_ID = 840001L;
    private static final long PROBLEM_ID = 840001L;
    private static final long TEST_CASE_ID_START = 840001L;


    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    private final String password;

    public SubmissionExternalCallLoadTestSeeder(
            JdbcTemplate jdbc,
            PasswordEncoder passwordEncoder,
            @Value("${loadtest.submission.password:}") String password
    ) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.password = password;

        if (password.isBlank()) {
            throw new IllegalArgumentException("loadtest.submission.password 설정이 필요합니다.");
        }
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long startNanos = System.nanoTime();

        seedUsers();
        seedTrustedDevices();
        seedCategory();
        seedProblemSet();
        seedProblem();

        clearPreviousSubmissionData();
        clearProgresses();
        seedTestCases();
        disableDatasets();
        seedProgresses();

        log.info(
                "event=submission_external_call_loadtest_seed_completed "
                        + "users={} problemSetId={} problemId={} testCases={} durationMs={}",
                USER_COUNT,
                PROBLEM_SET_ID,
                PROBLEM_ID,
                TEST_CASE_COUNT,
                elapsedMillis(startNanos)
        );
    }

    private void seedUsers() {
        String passwordHash = passwordEncoder.encode(password);

        jdbc.batchUpdate("""
                insert into users
                  (user_id, role, email, password, name, nickname, provider,
                   email_verified, is_locked, created_at, updated_at)
                values
                  (?, 'STUDENT', ?, ?, ?, ?, 'LOCAL',
                   true, false, now(6), now(6))
                on duplicate key update
                  role = values(role),
                  email = values(email),
                  password = values(password),
                  name = values(name),
                  nickname = values(nickname),
                  email_verified = true,
                  is_locked = false,
                  updated_at = now(6),
                  deleted_at = null
                """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i)
                            throws SQLException {
                        long userId = USER_ID_START + i;

                        ps.setLong(1, userId);
                        ps.setString(
                                2,
                                "submission-loadtest-" + userId + "@test.com"
                        );
                        ps.setString(3, passwordHash);
                        ps.setString(4, "SubmissionUser" + (i + 1));
                        ps.setString(5, "submission-loadtest-" + userId);
                    }

                    @Override
                    public int getBatchSize() {
                        return USER_COUNT;
                    }
                }
        );
    }

    private void seedTrustedDevices() {
        jdbc.update("""
                delete from trusted_devices
                 where user_id between ? and ?
                """,
                USER_ID_START,
                USER_ID_START + USER_COUNT - 1
        );

        jdbc.batchUpdate("""
                insert into trusted_devices
                  (user_id, device_fp, device_name, last_country, last_city,
                   last_used_at, created_at)
                values
                  (?, sha2(?, 256), 'submission-loadtest-device', null, null,
                   now(6), now(6))
                """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i)
                            throws SQLException {
                        long userId = USER_ID_START + i;

                        ps.setLong(1, userId);
                        ps.setString(
                                2,
                                "submission-loadtest-device-" + userId
                        );
                    }

                    @Override
                    public int getBatchSize() {
                        return USER_COUNT;
                    }
                }
        );
    }

    private void seedCategory() {
        jdbc.update("""
                insert into problem_category
                  (category_id, category_name, description, status)
                values
                  (?, 'Submission Loadtest',
                   'Submission external call bottleneck test', 'ACTIVE')
                on duplicate key update
                  category_name = values(category_name),
                  description = values(description),
                  status = values(status)
                """,
                CATEGORY_ID
        );
    }

    private void seedProblemSet() {
        jdbc.update("""
                insert into problem_set
                  (problem_set_id, category_id, title, description,
                   difficulty, status, total_problem_count,
                   completed_user_count, started_user_count,
                   created_by, created_at, deleted_at)
                values
                  (?, ?, 'Submission External Call Loadtest',
                   'Transaction external call bottleneck test',
                   'EASY', 'ACTIVE', 1, 0, ?, ?, now(6), null)
                on duplicate key update
                  category_id = values(category_id),
                  title = values(title),
                  description = values(description),
                  difficulty = values(difficulty),
                  status = values(status),
                  total_problem_count = 1,
                  completed_user_count = 0,
                  started_user_count = values(started_user_count),
                  created_by = values(created_by),
                  deleted_at = null
                """,
                PROBLEM_SET_ID,
                CATEGORY_ID,
                USER_COUNT,
                USER_ID_START
        );
    }

    private void seedProblem() {
        jdbc.update("""
                insert into problem
                  (problem_id, problem_set_id, title, content,
                   problem_type, difficulty, explanation, point,
                   attempt_limit, is_retriable, status, problem_order)
                values
                  (?, ?, 'Submission Loadtest Problem',
                   'External call transaction test problem',
                   'CODE', 'EASY', 'Loadtest explanation',
                   10, 1000, true, 'ACTIVE', 1)
                on duplicate key update
                  problem_set_id = values(problem_set_id),
                  title = values(title),
                  content = values(content),
                  problem_type = values(problem_type),
                  difficulty = values(difficulty),
                  explanation = values(explanation),
                  point = values(point),
                  attempt_limit = values(attempt_limit),
                  is_retriable = values(is_retriable),
                  status = values(status),
                  problem_order = values(problem_order)
                """,
                PROBLEM_ID,
                PROBLEM_SET_ID
        );
    }

    private void clearPreviousSubmissionData() {
        jdbc.update("""
                delete result
                  from submission_test_result result
                  join submission submission
                    on submission.submission_id = result.submission_id
                 where submission.problem_id = ?
                """,
                PROBLEM_ID
        );

        jdbc.update("""
                delete from submission
                 where problem_id = ?
                """,
                PROBLEM_ID
        );

        jdbc.update("""
                delete from problem_test_case
                 where problem_id = ?
                """,
                PROBLEM_ID
        );
    }

    private void seedTestCases() {
        jdbc.batchUpdate("""
                insert into problem_test_case
                  (test_case_id, problem_id, test_code, test_order,
                   is_hidden, timeout_ms, status, created_at, updated_at)
                values
                  (?, ?, ?, ?, ?, 5000, 'ACTIVE', now(6), now(6))
                """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i)
                            throws SQLException {
                        ps.setLong(1, TEST_CASE_ID_START + i);
                        ps.setLong(2, PROBLEM_ID);
                        ps.setString(3, "# submission loadtest case " + (i + 1));
                        ps.setInt(4, i + 1);
                        ps.setBoolean(5, i > 1);
                    }

                    @Override
                    public int getBatchSize() {
                        return TEST_CASE_COUNT;
                    }
                }
        );
    }

    private void disableDatasets() {
        jdbc.update("""
                update problem_dataset
                   set status = 'INACTIVE',
                       updated_at = now(6)
                 where problem_set_id = ?
                   and status = 'ACTIVE'
                """,
                PROBLEM_SET_ID
        );
    }
    private void clearProgresses() {
        jdbc.update("""
                delete from problem_progress
                 where problem_set_id = ?
                   and user_id between ? and ?
                """,
                PROBLEM_SET_ID,
                USER_ID_START,
                USER_ID_START + USER_COUNT - 1
        );
    }

    private void seedProgresses() {
        jdbc.batchUpdate("""
                insert into problem_progress
                  (user_id, problem_set_id, current_problem_number,
                   is_completed, completed_at, updated_at)
                values
                  (?, ?, 1, false, null, now(6))
                on duplicate key update
                  current_problem_number = 1,
                  is_completed = false,
                  completed_at = null,
                  updated_at = now(6)
                """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i)
                            throws SQLException {
                        ps.setLong(1, USER_ID_START + i);
                        ps.setLong(2, PROBLEM_SET_ID);
                    }

                    @Override
                    public int getBatchSize() {
                        return USER_COUNT;
                    }
                }
        );
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
