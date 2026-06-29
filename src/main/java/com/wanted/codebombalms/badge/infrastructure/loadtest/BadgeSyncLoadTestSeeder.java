package com.wanted.codebombalms.badge.infrastructure.loadtest;

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
public class BadgeSyncLoadTestSeeder implements ApplicationRunner {

    private static final int USER_COUNT = 300;
    private static final int BADGE_COUNT = 100;
    private static final long USER_ID_START = 810001L;
    private static final long BADGE_ID_START = 810001L;
    private static final int USER_TOTAL_POINT = 1_000_000;
    private static final String PASSWORD = "Test1234!";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long startNanos = System.nanoTime();

        seedUsers();
        seedUserPoints();
        seedBadges();
        clearUserBadges();

        log.info(
                "event=badge_sync_loadtest_seed_completed users={} badges={} userTotalPoint={} durationMs={}",
                USER_COUNT,
                BADGE_COUNT,
                USER_TOTAL_POINT,
                elapsedMillis(startNanos)
        );
    }

    private void seedUsers() {
        String hash = passwordEncoder.encode(PASSWORD);

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
                ps.setString(2, "badge-sync-loadtest-" + userId + "@test.com");
                ps.setString(3, hash);
                ps.setString(4, "BS User " + userId);
                ps.setString(5, "badge-sync-" + userId);
            }

            @Override
            public int getBatchSize() {
                return USER_COUNT;
            }
        });
    }

    private void seedUserPoints() {
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
                ps.setLong(1, USER_ID_START + i);
                ps.setInt(2, USER_TOTAL_POINT);
            }

            @Override
            public int getBatchSize() {
                return USER_COUNT;
            }
        });
    }

    private void seedBadges() {
        jdbc.batchUpdate("""
                insert into badge
                  (badge_id, badge_name, description, required_point,
                   original_file_name, object_name, content_type, file_size,
                   status, created_at, updated_at, deleted_at)
                values
                  (?, ?, ?, ?, ?, ?, 'image/png', 1024, 'ACTIVE', now(6), now(6), null)
                on duplicate key update
                  badge_name = values(badge_name),
                  description = values(description),
                  required_point = values(required_point),
                  original_file_name = values(original_file_name),
                  object_name = values(object_name),
                  content_type = values(content_type),
                  file_size = values(file_size),
                  status = 'ACTIVE',
                  updated_at = now(6),
                  deleted_at = null
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                long badgeId = BADGE_ID_START + i;
                int badgeNumber = i + 1;

                ps.setLong(1, badgeId);
                ps.setString(2, "Badge Sync Loadtest " + badgeNumber);
                ps.setString(3, "Badge sync loadtest badge");
                ps.setInt(4, badgeNumber * 100);
                ps.setString(5, "badge-sync-loadtest-" + badgeNumber + ".png");
                ps.setString(6, "badge/loadtest-sync-badge-" + badgeNumber + ".png");
            }

            @Override
            public int getBatchSize() {
                return BADGE_COUNT;
            }
        });
    }

    private void clearUserBadges() {
        jdbc.update("""
                delete from user_badge
                 where user_id between ? and ?
                   and badge_id between ? and ?
                """,
                USER_ID_START,
                USER_ID_START + USER_COUNT - 1,
                BADGE_ID_START,
                BADGE_ID_START + BADGE_COUNT - 1
        );
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
