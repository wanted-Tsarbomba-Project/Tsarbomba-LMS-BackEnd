package com.wanted.codebombalms.chatbot.infrastructure.loadtest;

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
import java.util.List;

/**
 * 트랙 A(GET /api/v1/chat/list N+1) baseline 용 부하테스트 시드.
 *
 * <p>{@code loadtest} 프로파일에서만, 부팅(테이블 생성) 직후 1회 실행한다.
 * 로그인 계정 1명 + 그 계정 소유 채팅방을 대량으로 만들어, 방마다 problem 제목 2회 조회(N+1)가
 * 실제로 터지도록 한다. 방이 적으면 N+1 이 안 드러나므로 {@link #ROOMS} 가 핵심 파라미터다.
 *
 * <p>raw JdbcTemplate 으로 cross-domain 테이블(users·problem_set·problem·chat_room)을 직접 채운다.
 * 런타임 BC 경계 위반이 아니라 테스트 스캐폴딩이다.
 */
@Slf4j
@Component
@Profile("loadtest")
@RequiredArgsConstructor
public class ChatListLoadTestSeeder implements ApplicationRunner {

    // === 튜닝 파라미터 ===
    private static final String LOGIN_EMAIL = "u01@test.com";   // auth.js 기본 계정과 일치
    private static final String LOGIN_PASSWORD = "Test1234!";
    private static final int PROBLEM_SETS = 10;
    private static final int PROBLEMS = 200;
    private static final int ROOMS = 200;   // /list 1회 = 1 + 2*ROOMS 쿼리

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Long already = jdbc.queryForObject(
                "select count(*) from users where email = ?", Long.class, LOGIN_EMAIL);
        if (already != null && already > 0) {
            log.info("event=loadtest_seed_skipped reason=already_seeded email={}", LOGIN_EMAIL);
            return;
        }

        long startedAt = System.nanoTime();

        Long userId = seedLoginUser();
        List<Long> setIds = seedProblemSets(userId);
        List<Long> problemIds = seedProblems(setIds);
        seedChatRooms(userId, setIds, problemIds);

        log.info("event=loadtest_seed_completed userId={} sets={} problems={} rooms={} durationMs={}",
                userId, setIds.size(), problemIds.size(), ROOMS,
                (System.nanoTime() - startedAt) / 1_000_000);
    }

    private Long seedLoginUser() {
        String hash = passwordEncoder.encode(LOGIN_PASSWORD);
        jdbc.update("""
                insert into users
                  (role, email, password, name, nickname, provider, email_verified, is_locked, created_at, updated_at)
                values
                  ('STUDENT', ?, ?, '부하테스트', 'loadtester', 'LOCAL', true, false, now(6), now(6))
                """, LOGIN_EMAIL, hash);
        return jdbc.queryForObject("select user_id from users where email = ?", Long.class, LOGIN_EMAIL);
    }

    private List<Long> seedProblemSets(Long userId) {
        jdbc.batchUpdate("""
                insert into problem_set
                  (title, status, total_problem_count, completed_user_count, started_user_count, created_by, created_at)
                values (?, 'ACTIVE', 0, 0, 0, ?, now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, "부하셋 " + (i + 1));
                ps.setLong(2, userId);
            }

            @Override
            public int getBatchSize() {
                return PROBLEM_SETS;
            }
        });
        return jdbc.queryForList(
                "select problem_set_id from problem_set order by problem_set_id", Long.class);
    }

    private List<Long> seedProblems(List<Long> setIds) {
        jdbc.batchUpdate("""
                insert into problem (problem_set_id, title, status, point)
                values (?, ?, 'ACTIVE', 0)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, setIds.get(i % setIds.size()));
                ps.setString(2, "부하문제 " + (i + 1));
            }

            @Override
            public int getBatchSize() {
                return PROBLEMS;
            }
        });
        return jdbc.queryForList(
                "select problem_id from problem order by problem_id", Long.class);
    }

    private void seedChatRooms(Long userId, List<Long> setIds, List<Long> problemIds) {
        jdbc.batchUpdate("""
                insert into chat_room (user_id, problem_set_id, problem_id, title, created_at, updated_at)
                values (?, ?, ?, ?, now(6), now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // problem_id 는 행마다 고유 → (user, set, problem) 유니크 제약 자동 충족
                ps.setLong(1, userId);
                ps.setLong(2, setIds.get(i % setIds.size()));
                ps.setLong(3, problemIds.get(i % problemIds.size()));
                ps.setString(4, "부하 채팅방 " + (i + 1));
            }

            @Override
            public int getBatchSize() {
                return ROOMS;
            }
        });
    }
}
