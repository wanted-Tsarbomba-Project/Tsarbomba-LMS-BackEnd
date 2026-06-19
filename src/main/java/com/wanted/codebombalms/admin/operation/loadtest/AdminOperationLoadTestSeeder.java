package com.wanted.codebombalms.admin.operation.loadtest;

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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin 운영 알림 목록/상세 baseline 용 부하테스트 시드.
 *
 * <p>{@code loadtest} 프로파일에서만 실행한다. admin 로그인 계정과 RULE_MANAGEMENT 권한,
 * 상세 조회 target 이 실제로 바라볼 COURSE/PROBLEM/USER 데이터, 운영 알림 200건을 만든다.
 */
@Slf4j
@Component
@Profile("loadtest")
@DependsOn("chatListLoadTestSeeder")
@RequiredArgsConstructor
public class AdminOperationLoadTestSeeder implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String ADMIN_PASSWORD = "Test1234!";
    private static final int ALERTS = 200;
    private static final int COURSES = 40;
    private static final int PROBLEMS = 80;
    private static final int USERS = 240;
    private static final int SUBMISSIONS_PER_PROBLEM = 25;

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Long already = jdbc.queryForObject("select count(*) from operation_alert", Long.class);
        if (already != null && already > 0) {
            log.info("event=loadtest_admin_seed_skipped reason=already_seeded alerts={}", already);
            return;
        }

        long startedAt = System.nanoTime();

        Long adminUserId = seedAdminUser();
        seedAdminPermission(adminUserId);
        seedAutomationRules();

        Long courseCategoryId = seedCourseCategory();
        List<Long> courseIds = seedCourses(adminUserId, courseCategoryId);
        List<Long> targetUserIds = seedTargetUsers();
        List<Long> problemIds = loadProblemIds();

        seedOldLoginHistory(targetUserIds);
        seedLowEnrollments(courseIds, targetUserIds);
        seedWrongRateSubmissions(problemIds, targetUserIds);
        seedOperationAlerts(courseIds, problemIds, targetUserIds);

        log.info("event=loadtest_admin_seed_completed adminUserId={} alerts={} courses={} problems={} users={} submissions={} durationMs={}",
                adminUserId, ALERTS, courseIds.size(), problemIds.size(), targetUserIds.size(),
                problemIds.size() * SUBMISSIONS_PER_PROBLEM,
                (System.nanoTime() - startedAt) / 1_000_000);
    }

    private Long seedAdminUser() {
        Long exists = jdbc.queryForObject("select count(*) from users where email = ?", Long.class, ADMIN_EMAIL);
        if (exists != null && exists > 0) {
            return jdbc.queryForObject("select user_id from users where email = ?", Long.class, ADMIN_EMAIL);
        }

        jdbc.update("""
                insert into users
                  (role, email, password, name, nickname, provider, email_verified, is_locked, created_at, updated_at)
                values
                  ('ADMIN', ?, ?, '부하 관리자', 'loadtest-admin', 'LOCAL', true, false, now(6), now(6))
                """, ADMIN_EMAIL, passwordEncoder.encode(ADMIN_PASSWORD));

        return jdbc.queryForObject("select user_id from users where email = ?", Long.class, ADMIN_EMAIL);
    }

    private void seedAdminPermission(Long adminUserId) {
        jdbc.update("""
                insert into admin_permissions
                  (admin_user_id, permission_type, granted_by, created_at)
                values (?, 'RULE_MANAGEMENT', ?, now(6))
                """, adminUserId, adminUserId);
    }

    private void seedAutomationRules() {
        jdbc.update("""
                insert into automation_rule
                  (rule_code, threshold_value, min_sample_count, severity, enabled, created_at)
                values
                  ('COURSE_LOW_ENROLLMENT', 10.00, null, 'MEDIUM', true, now(6)),
                  ('USER_INACTIVE_NO_COURSE', 30.00, null, 'LOW', true, now(6)),
                  ('PROBLEM_HIGH_WRONG_RATE', 70.00, 20, 'HIGH', true, now(6))
                """);
    }

    private Long seedCourseCategory() {
        jdbc.update("""
                insert into course_category (name, status, display_order, created_at)
                values ('운영 부하 카테고리', 'ACTIVE', 999, now(6))
                """);
        return jdbc.queryForObject("""
                select course_category_id
                from course_category
                where name = '운영 부하 카테고리'
                """, Long.class);
    }

    private List<Long> seedCourses(Long instructorId, Long courseCategoryId) {
        jdbc.batchUpdate("""
                insert into course
                  (instructor_id, course_category_id, title, description, thumbnail_url, status, created_at)
                values (?, ?, ?, 'admin operation loadtest course', null, 'ACTIVE', now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, instructorId);
                ps.setLong(2, courseCategoryId);
                ps.setString(3, "운영 부하 강좌 " + (i + 1));
            }

            @Override
            public int getBatchSize() {
                return COURSES;
            }
        });

        return jdbc.queryForList("""
                select course_id
                from course
                where title like '운영 부하 강좌 %'
                order by course_id
                """, Long.class);
    }

    private List<Long> seedTargetUsers() {
        jdbc.batchUpdate("""
                insert into users
                  (role, email, password, name, nickname, provider, email_verified, is_locked, created_at, updated_at)
                values
                  ('STUDENT', ?, ?, ?, ?, 'LOCAL', true, false, date_sub(now(6), interval 45 day), now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int no = i + 1;
                ps.setString(1, "admin-target-" + no + "@test.com");
                ps.setString(2, passwordEncoder.encode(ADMIN_PASSWORD));
                ps.setString(3, "운영대상 " + no);
                ps.setString(4, "admin-target-" + no);
            }

            @Override
            public int getBatchSize() {
                return USERS;
            }
        });

        return jdbc.queryForList("""
                select user_id
                from users
                where email like 'admin-target-%@test.com'
                order by user_id
                """, Long.class);
    }

    private List<Long> loadProblemIds() {
        return jdbc.queryForList("""
                select problem_id
                from problem
                order by problem_id
                limit ?
                """, Long.class, PROBLEMS);
    }

    private void seedOldLoginHistory(List<Long> targetUserIds) {
        jdbc.batchUpdate("""
                insert into login_history
                  (user_id, ip_address, user_agent, device_fp, country, city, is_suspicious, created_at)
                values (?, '127.0.0.1', 'loadtest-admin-automation', null, 'KR', 'Seoul', false,
                        date_sub(now(6), interval ? day))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, targetUserIds.get(i));
                ps.setInt(2, 35 + (i % 20));
            }

            @Override
            public int getBatchSize() {
                return targetUserIds.size();
            }
        });
    }

    private void seedLowEnrollments(List<Long> courseIds, List<Long> targetUserIds) {
        int enrollmentCount = courseIds.size() * 3;
        jdbc.batchUpdate("""
                insert into enrollment
                  (user_id, course_id, status, enrolled_at, canceled_at)
                values (?, ?, 'ACTIVE', date_sub(now(6), interval ? day), null)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, targetUserIds.get(i % targetUserIds.size()));
                ps.setLong(2, courseIds.get(i / 3));
                ps.setInt(3, 20 + (i % 10));
            }

            @Override
            public int getBatchSize() {
                return enrollmentCount;
            }
        });
    }

    private void seedWrongRateSubmissions(List<Long> problemIds, List<Long> targetUserIds) {
        int submissionCount = problemIds.size() * SUBMISSIONS_PER_PROBLEM;
        jdbc.batchUpdate("""
                insert into submission
                  (user_id, problem_id, submitted_code, is_correct, attempt_no,
                   passed_test_count, total_test_count, execution_status, error_message, submitted_at)
                values (?, ?, 'print("loadtest")', ?, 1, ?, 10, 'COMPLETED', null,
                        date_sub(now(6), interval ? minute))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                boolean correct = i % 5 == 0;
                ps.setLong(1, targetUserIds.get(i % targetUserIds.size()));
                ps.setLong(2, problemIds.get(i / SUBMISSIONS_PER_PROBLEM));
                ps.setBoolean(3, correct);
                ps.setInt(4, correct ? 10 : 3);
                ps.setInt(5, submissionCount - i);
            }

            @Override
            public int getBatchSize() {
                return submissionCount;
            }
        });
    }

    private void seedOperationAlerts(List<Long> courseIds, List<Long> problemIds, List<Long> targetUserIds) {
        List<Long> ruleIds = jdbc.queryForList("""
                select operation_rule_id
                from automation_rule
                order by operation_rule_id
                """, Long.class);

        jdbc.batchUpdate("""
                insert into operation_alert
                  (operation_rule_id, target_type, target_id, detected_value, threshold_value_snapshot,
                   assignee_id, reason, recommended_action, first_detected_at, last_detected_at,
                   status, resolved_by, resolved_at, admin_memo, created_at, updated_at, deleted_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, null, null, null, ?, null, null)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                AlertTarget target = alertTarget(i, ruleIds, courseIds, problemIds, targetUserIds);
                LocalDateTime detectedAt = LocalDateTime.now().minusMinutes(ALERTS - i);

                ps.setLong(1, target.ruleId());
                ps.setString(2, target.targetType());
                ps.setLong(3, target.targetId());
                ps.setBigDecimal(4, target.detectedValue());
                ps.setBigDecimal(5, target.thresholdValue());
                ps.setLong(6, target.assigneeId());
                ps.setString(7, "loadtest alert reason " + (i + 1));
                ps.setString(8, "loadtest recommended action " + (i + 1));
                ps.setTimestamp(9, Timestamp.valueOf(detectedAt.minusMinutes(5)));
                ps.setTimestamp(10, Timestamp.valueOf(detectedAt));
                ps.setString(11, i % 7 == 0 ? "RESOLVED" : "OPEN");
                ps.setTimestamp(12, Timestamp.valueOf(detectedAt));
            }

            @Override
            public int getBatchSize() {
                return ALERTS;
            }
        });
    }

    private AlertTarget alertTarget(
            int index,
            List<Long> ruleIds,
            List<Long> courseIds,
            List<Long> problemIds,
            List<Long> targetUserIds
    ) {
        int type = index % 3;
        if (type == 0) {
            return new AlertTarget(
                    ruleIds.get(0),
                    "COURSE",
                    courseIds.get(index % courseIds.size()),
                    BigDecimal.valueOf(3),
                    BigDecimal.valueOf(10),
                    targetUserIds.get(index % targetUserIds.size())
            );
        }
        if (type == 1) {
            return new AlertTarget(
                    ruleIds.get(1),
                    "USER",
                    targetUserIds.get(index % targetUserIds.size()),
                    BigDecimal.valueOf(45),
                    BigDecimal.valueOf(30),
                    targetUserIds.get(index % targetUserIds.size())
            );
        }

        return new AlertTarget(
                ruleIds.get(2),
                "PROBLEM",
                problemIds.get(index % problemIds.size()),
                BigDecimal.valueOf(82),
                BigDecimal.valueOf(70),
                targetUserIds.get(index % targetUserIds.size())
        );
    }

    private record AlertTarget(
            Long ruleId,
            String targetType,
            Long targetId,
            BigDecimal detectedValue,
            BigDecimal thresholdValue,
            Long assigneeId
    ) {
    }
}
