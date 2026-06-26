package com.wanted.codebombalms.problems.set.infrastructure.loadtest;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("loadtest & !loadtest-admin")
@RequiredArgsConstructor
public class ProblemSetEntryLoadTestSeeder implements ApplicationRunner {

    private static final int USER_COUNT = 300;
    private static final int PROBLEM_COUNT = 20;
    private static final int SUBMISSIONS_PER_PROBLEM = 2;
    private static final long USER_ID_START = 830001L;
    private static final long CATEGORY_ID = 830001L;
    private static final long PROBLEM_SET_ID = 830001L;
    private static final long PROBLEM_ID_START = 830001L;
    private static final long DATASET_ID = 830001L;
    private static final long AUTHOR_ID = 830000L;
    private static final String PASSWORD = "Test1234!";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long startNanos = System.nanoTime();

        seedUsers();
        seedCategory();
        seedProblemSet();
        seedDataset();
        seedProblems();
        seedProgresses();
        clearSubmissions();
        seedSubmissions();

        log.info(
                "event=problem_set_entry_loadtest_seed_completed users={} problemSetId={} problems={} "
                        + "submissionsPerProblem={} durationMs={}",
                USER_COUNT,
                PROBLEM_SET_ID,
                PROBLEM_COUNT,
                SUBMISSIONS_PER_PROBLEM,
                elapsedMillis(startNanos)
        );
    }

    private void seedUsers() {
        String hash = passwordEncoder.encode(PASSWORD);

        jdbc.update("""
                insert into users
                  (user_id, role, email, password, name, nickname, provider,
                   email_verified, is_locked, created_at, updated_at)
                values
                  (?, 'OPERATOR', ?, ?, 'Entry Operator', 'problem-entry-operator',
                   'LOCAL', true, false, now(6), now(6))
                on duplicate key update
                  role = values(role),
                  password = values(password),
                  name = values(name),
                  nickname = values(nickname),
                  updated_at = now(6),
                  deleted_at = null
                """, AUTHOR_ID, "problem-entry-operator@test.com", hash);

        jdbc.batchUpdate("""
                insert into users
                  (user_id, role, email, password, name, nickname, provider,
                   email_verified, is_locked, created_at, updated_at)
                values
                  (?, 'STUDENT', ?, ?, ?, ?, 'LOCAL', true, false, now(6), now(6))
                on duplicate key update
                  role = values(role),
                  password = values(password),
                  name = values(name),
                  nickname = values(nickname),
                  updated_at = now(6),
                  deleted_at = null
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                long userId = USER_ID_START + i;

                ps.setLong(1, userId);
                ps.setString(2, "problem-entry-loadtest-" + userId + "@test.com");
                ps.setString(3, hash);
                ps.setString(4, "EntryUser" + (i + 1));
                ps.setString(5, "problem-entry-" + userId);
            }

            @Override
            public int getBatchSize() {
                return USER_COUNT;
            }
        });
    }

    private void seedCategory() {
        jdbc.update("""
                insert into problem_category
                  (category_id, category_name, description, status)
                values
                  (?, 'Problem Set Entry Loadtest', 'Problem set entry bottleneck loadtest category', 'ACTIVE')
                on duplicate key update
                  category_name = values(category_name),
                  description = values(description),
                  status = values(status)
                """, CATEGORY_ID);
    }

    private void seedProblemSet() {
        jdbc.update("""
                insert into problem_set
                  (problem_set_id, category_id, title, description, difficulty, status,
                   total_problem_count, completed_user_count, started_user_count, created_by, created_at, deleted_at)
                values
                  (?, ?, 'Problem Set Entry Loadtest',
                   'Problem set entry API bottleneck loadtest set',
                   'EASY', 'ACTIVE', ?, 0, ?, ?, now(6), null)
                on duplicate key update
                  category_id = values(category_id),
                  title = values(title),
                  description = values(description),
                  difficulty = values(difficulty),
                  status = values(status),
                  total_problem_count = values(total_problem_count),
                completed_user_count = values(completed_user_count),
                  started_user_count = values(started_user_count),
                  created_by = values(created_by),
                  deleted_at = null
                """, PROBLEM_SET_ID, CATEGORY_ID, PROBLEM_COUNT, USER_COUNT, AUTHOR_ID);
    }

    private void seedDataset() {
        jdbc.update("""
                insert into problem_dataset
                  (dataset_id, problem_set_id, original_file_name, stored_file_name, file_url,
                   file_path, file_size, status, created_at, updated_at)
                values
                  (?, ?, 'problem-entry-loadtest.csv', 'problem-entry-loadtest.csv',
                   'https://example.com/problem-entry-loadtest.csv',
                   'datasets/problem-entry-loadtest.csv', 1024, 'ACTIVE', now(6), now(6))
                on duplicate key update
                  problem_set_id = values(problem_set_id),
                  original_file_name = values(original_file_name),
                  stored_file_name = values(stored_file_name),
                  file_url = values(file_url),
                  file_path = values(file_path),
                  file_size = values(file_size),
                  status = values(status),
                  updated_at = now(6)
                """, DATASET_ID, PROBLEM_SET_ID);
    }

    private void seedProblems() {
        jdbc.batchUpdate("""
                insert into problem
                  (problem_id, problem_set_id, title, content, problem_type, difficulty,
                   explanation, point, attempt_limit, is_retriable, status, problem_order)
                values
                  (?, ?, ?, ?, 'CODE', 'EASY', ?, 10, 3, true, 'ACTIVE', ?)
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
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int problemNumber = i + 1;

                ps.setLong(1, PROBLEM_ID_START + i);
                ps.setLong(2, PROBLEM_SET_ID);
                ps.setString(3, "Problem Entry Loadtest Problem " + problemNumber);
                ps.setString(4, "Loadtest content for problem " + problemNumber);
                ps.setString(5, "Loadtest explanation for problem " + problemNumber);
                ps.setInt(6, problemNumber);
            }

            @Override
            public int getBatchSize() {
                return PROBLEM_COUNT;
            }
        });
    }

    private void seedProgresses() {
        jdbc.batchUpdate("""
                insert into problem_progress
                  (user_id, problem_set_id, current_problem_number, is_completed, completed_at, updated_at)
                values
                  (?, ?, ?, false, null, now(6))
                on duplicate key update
                  current_problem_number = values(current_problem_number),
                  is_completed = values(is_completed),
                  completed_at = null,
                  updated_at = now(6)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, USER_ID_START + i);
                ps.setLong(2, PROBLEM_SET_ID);
                ps.setInt(3, PROBLEM_COUNT);
            }

            @Override
            public int getBatchSize() {
                return USER_COUNT;
            }
        });
    }

    private void clearSubmissions() {
        jdbc.update("""
            delete s
              from submission s
              join problem p
                on p.problem_id = s.problem_id
             where s.user_id between ? and ?
               and p.problem_set_id = ?
            """,
                USER_ID_START,
                USER_ID_START + USER_COUNT - 1,
                PROBLEM_SET_ID
        );
    }



    private void seedSubmissions() {
        jdbc.batchUpdate("""
                insert into submission
                  (user_id, problem_id, submitted_code, is_correct, attempt_no,
                   passed_test_count, total_test_count, execution_status, error_message, submitted_at)
                values
                  (?, ?, ?, ?, ?, ?, 10, 'SUCCESS', null, timestampadd(second, ?, now(6)))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int perUser = PROBLEM_COUNT * SUBMISSIONS_PER_PROBLEM;
                int userIndex = i / perUser;
                int localIndex = i % perUser;
                int problemIndex = localIndex / SUBMISSIONS_PER_PROBLEM;
                int attemptNo = (localIndex % SUBMISSIONS_PER_PROBLEM) + 1;
                boolean latestAttempt = attemptNo == SUBMISSIONS_PER_PROBLEM;
                boolean correct = latestAttempt && problemIndex % 3 != 0;

                ps.setLong(1, USER_ID_START + userIndex);
                ps.setLong(2, PROBLEM_ID_START + problemIndex);
                ps.setString(3, "print('problem-entry-loadtest')");
                ps.setBoolean(4, correct);
                ps.setInt(5, attemptNo);
                ps.setInt(6, correct ? 10 : 3);
                ps.setInt(7, -(perUser - localIndex));
            }

            @Override
            public int getBatchSize() {
                return USER_COUNT * PROBLEM_COUNT * SUBMISSIONS_PER_PROBLEM;
            }
        });
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
