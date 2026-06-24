package com.wanted.codebombalms.reward.point.infrastructure.loadtest;

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

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@Component
@Profile("loadtest & !loadtest-admin")
@RequiredArgsConstructor
public class RankingBadgeLoadTestSeeder implements ApplicationRunner {

    private static final String LOGIN_EMAIL = "u01@test.com";
    private static final String LOGIN_PASSWORD = "Test1234!";

    private static final int USER_COUNT = 300;
    private static final int POINT_HISTORY_PER_USER = 20;
    private static final int BADGE_COUNT = 20;
    private static final int RANKING_BADGE_EQUIPPED_USER_COUNT = USER_COUNT;

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long alreadySeeded = jdbc.queryForObject(
                "select count(*) from user_point",
                Long.class
        );

        long startNanos = System.nanoTime();
        Long loginUserId = findOrCreateLoginUser();

        if (alreadySeeded != null && alreadySeeded >= USER_COUNT) {
            log.info(
                    "event=ranking_badge_loadtest_seed_skipped reason=already_seeded userPointCount={}",
                    alreadySeeded
            );
        } else {
            seedRankingUsers();
            seedUserPoints(loginUserId);
            seedPointHistories(loginUserId);
        }

        seedBadges();
        seedRankingUserBadges();
        seedLoginUserBadges(loginUserId);

        log.info(
                "event=ranking_badge_loadtest_seed_completed users={} historiesPerUser={} badges={} equippedRankingUsers={} durationMs={}",
                USER_COUNT,
                POINT_HISTORY_PER_USER,
                BADGE_COUNT,
                RANKING_BADGE_EQUIPPED_USER_COUNT,
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
                  (role, email, password, name, nickname, provider,
                   email_verified, is_locked, created_at, updated_at)
                values
                  ('STUDENT', ?, ?, 'Loadtest User', 'loadtester-ranking',
                   'LOCAL', true, false, now(6), now(6))
                """, LOGIN_EMAIL, hash);

        return jdbc.queryForObject(
                "select user_id from users where email = ?",
                Long.class,
                LOGIN_EMAIL
        );
    }

    private void seedRankingUsers() {
        String hash = passwordEncoder.encode(LOGIN_PASSWORD);

        jdbc.batchUpdate("""
                insert ignore into users
                  (user_id, role, email, password, name, nickname, provider,
                   email_verified, is_locked, created_at, updated_at)
                values
                  (?, 'STUDENT', ?, ?, ?, ?, 'LOCAL', true, false, now(6), now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                long userId = 700001L + i;

                ps.setLong(1, userId);
                ps.setString(2, "ranking-loadtest-" + userId + "@test.com");
                ps.setString(3, hash);
                ps.setString(4, "Ranking User " + userId);
                ps.setString(5, "ranking-" + userId);
            }

            @Override
            public int getBatchSize() {
                return USER_COUNT;
            }
        });
    }

    private void seedUserPoints(Long loginUserId) {
        jdbc.batchUpdate("""
                insert into user_point
                  (user_id, total_point, created_at, updated_at)
                values
                  (?, ?, now(6), now(6))
                on duplicate key update
                  total_point = values(total_point),
                  updated_at = now(6)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                long userId = 700001L + i;
                int totalPoint = 1000 + (USER_COUNT - i) * 10;

                ps.setLong(1, userId);
                ps.setInt(2, totalPoint);
            }

            @Override
            public int getBatchSize() {
                return USER_COUNT;
            }
        });

        jdbc.update("""
                insert into user_point
                  (user_id, total_point, created_at, updated_at)
                values
                  (?, 5000, now(6), now(6))
                on duplicate key update
                  total_point = values(total_point),
                  updated_at = now(6)
                """, loginUserId);
    }

    private void seedPointHistories(Long loginUserId) {
        jdbc.batchUpdate("""
                insert ignore into point_history
                  (user_id, problem_id, submission_id, point, reason, created_at)
                values
                  (?, ?, ?, ?, 'LOADTEST', now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int userIndex = i / POINT_HISTORY_PER_USER;
                int historyIndex = i % POINT_HISTORY_PER_USER;

                long userId = 700001L + userIndex;
                long problemId = 5001L + historyIndex;
                long submissionId = 900000000L + (userIndex * 100L) + historyIndex;
                int point = 10 + (historyIndex % 5) * 5;

                ps.setLong(1, userId);
                ps.setLong(2, problemId);
                ps.setLong(3, submissionId);
                ps.setInt(4, point);
            }

            @Override
            public int getBatchSize() {
                return USER_COUNT * POINT_HISTORY_PER_USER;
            }
        });

        jdbc.batchUpdate("""
                insert ignore into point_history
                  (user_id, problem_id, submission_id, point, reason, created_at)
                values
                  (?, ?, ?, 50, 'LOADTEST_LOGIN_USER', now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, loginUserId);
                ps.setLong(2, 5001L + i);
                ps.setLong(3, 990000000L + i);
            }

            @Override
            public int getBatchSize() {
                return POINT_HISTORY_PER_USER;
            }
        });
    }

    private void seedBadges() {
        jdbc.batchUpdate("""
                insert ignore into badge
                  (badge_id, badge_name, description, required_point,
                   original_file_name, object_name, content_type, file_size,
                   status, created_at, updated_at, deleted_at)
                values
                  (?, ?, ?, ?, ?, ?, 'image/png', 1024, 'ACTIVE', now(6), now(6), null)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                long badgeId = 800001L + i;
                int requiredPoint = (i + 1) * 100;

                ps.setLong(1, badgeId);
                ps.setString(2, "Loadtest Badge " + (i + 1));
                ps.setString(3, "Ranking and badge loadtest badge");
                ps.setInt(4, requiredPoint);
                ps.setString(5, "loadtest-badge-" + (i + 1) + ".png");
                ps.setString(6, "badge/loadtest-badge-" + (i + 1) + ".png");
            }

            @Override
            public int getBatchSize() {
                return BADGE_COUNT;
            }
        });
    }

    private void seedLoginUserBadges(Long loginUserId) {
        jdbc.update("""
        update user_badge
           set is_equipped = false
         where user_id = ?
        """, loginUserId);
        jdbc.batchUpdate("""
                insert into user_badge
                  (user_id, badge_id, earned_at, is_equipped)
                values
                  (?, ?, now(6), ?)
                on duplicate key update
                  is_equipped = values(is_equipped)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, loginUserId);
                ps.setLong(2, 800001L + i);
                ps.setBoolean(3, i == 0);
            }

            @Override
            public int getBatchSize() {
                return BADGE_COUNT;
            }
        });
    }

    private void seedRankingUserBadges() {
        jdbc.update("""
                update user_badge
                   set is_equipped = false
                 where user_id between 700001 and ?
                """, 700000L + RANKING_BADGE_EQUIPPED_USER_COUNT);

        jdbc.batchUpdate("""
                insert into user_badge
                  (user_id, badge_id, earned_at, is_equipped)
                values
                  (?, ?, now(6), true)
                on duplicate key update
                  is_equipped = true
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                long userId = 700001L + i;
                long badgeId = 800001L + (i % BADGE_COUNT);

                ps.setLong(1, userId);
                ps.setLong(2, badgeId);
            }

            @Override
            public int getBatchSize() {
                return RANKING_BADGE_EQUIPPED_USER_COUNT;
            }
        });
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
