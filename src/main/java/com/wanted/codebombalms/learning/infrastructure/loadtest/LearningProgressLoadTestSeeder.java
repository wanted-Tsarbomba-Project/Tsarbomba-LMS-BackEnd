package com.wanted.codebombalms.learning.infrastructure.loadtest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HexFormat;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("loadtest & loadtest-learning")
@Order(0)
@RequiredArgsConstructor
public class LearningProgressLoadTestSeeder implements ApplicationRunner {

    // === 튜닝 파라미터 ===
    // /users/learning-progress 1회는 학생마다 buildStudentProgress() 를 반복한다.
    // 학생 수가 핵심 병목 배율이고, 강의/문제세트 수는 학생별 count 쿼리의 IN 크기를 키운다.
    // 문서의 100~300명 범위 안에서 baseline 첫 측정용으로 200명을 사용한다.
    private static final long COURSE_CATEGORY_ID = 1000L;
    private static final long COURSE_ID = 2000L;
    private static final long ADMIN_USER_ID = 20000L;
    private static final long INSTRUCTOR_USER_ID = 20001L;
    private static final long STUDENT_ID_START = 20100L;
    private static final long LECTURE_ID_START = 21000L;
    private static final long PROBLEM_SET_ID_START = 22000L;
    private static final long LECTURE_PROBLEM_SET_ID_START = 23000L;

    private static final String ADMIN_EMAIL = "learning-loadtest-admin@test.com";
    private static final String OPERATOR_EMAIL = "learning-loadtest-operator@test.com";
    private static final int STUDENTS = 100000;
    private static final int LECTURES = 12;
    private static final int PROBLEM_SETS = 12;
    private static final String LOGIN_PASSWORD = "Test1234!";
    private static final String LOADTEST_DEVICE_ID = "learning-loadtest-device";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    void logBeanRegistered() {
        log.info("event=learning_loadtest_seed_bean_registered courseId={} students={}", COURSE_ID, STUDENTS);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("event=learning_loadtest_seed_started courseId={} students={}", COURSE_ID, STUDENTS);

        Long enrollmentCount = jdbc.queryForObject(
                "select count(*) from enrollment where course_id = ?",
                Long.class,
                COURSE_ID
        );
        if (enrollmentCount != null && enrollmentCount >= STUDENTS) {
            seedTrustedDevices();
            log.info("event=learning_loadtest_seed_skipped courseId={} studentCount={}", COURSE_ID, enrollmentCount);
            return;
        }

        long startedAt = System.nanoTime();

        seedUsers();
        log.info("event=learning_loadtest_seed_step_completed step=users students={}", STUDENTS);
        seedTrustedDevices();
        log.info("event=learning_loadtest_seed_step_completed step=trusted_devices");
        seedCourse();
        log.info("event=learning_loadtest_seed_step_completed step=course courseId={}", COURSE_ID);
        seedLectures();
        log.info("event=learning_loadtest_seed_step_completed step=lectures lectures={}", LECTURES);
        seedProblemSets();
        log.info("event=learning_loadtest_seed_step_completed step=problem_sets problemSets={}", PROBLEM_SETS);
        seedLectureProblemSets();
        log.info("event=learning_loadtest_seed_step_completed step=lecture_problem_sets");
        seedEnrollments();
        log.info("event=learning_loadtest_seed_step_completed step=enrollments students={}", STUDENTS);
        seedLectureProgresses();
        log.info("event=learning_loadtest_seed_step_completed step=lecture_progresses rows={}", STUDENTS * LECTURES);
        seedLectureProblemProgresses();
        log.info("event=learning_loadtest_seed_step_completed step=lecture_problem_progresses rows={}", STUDENTS * PROBLEM_SETS);

        log.info("event=learning_loadtest_seed_completed courseId={} students={} lectures={} problemSets={} durationMs={}",
                COURSE_ID, STUDENTS, LECTURES, PROBLEM_SETS, (System.nanoTime() - startedAt) / 1_000_000);
    }

    private void seedUsers() {
        String hash = passwordEncoder.encode(LOGIN_PASSWORD);

        jdbc.update("""
                insert into users
                  (user_id, role, email, password, name, nickname, provider, email_verified, is_locked,
                   created_at, updated_at)
                values
                  (?, 'ADMIN', ?, ?, '부하관리자', 'load-admin', 'LOCAL', true, false, now(6), now(6))
                on duplicate key update
                  role = values(role), password = values(password), updated_at = now(6), deleted_at = null
                """, ADMIN_USER_ID, ADMIN_EMAIL, hash);

        jdbc.update("""
                insert into users
                  (user_id, role, email, password, name, nickname, provider, email_verified, is_locked,
                   created_at, updated_at)
                values
                  (?, 'OPERATOR', ?, ?, '부하운영자', 'load-operator', 'LOCAL', true, false, now(6), now(6))
                on duplicate key update
                  role = values(role), password = values(password), updated_at = now(6), deleted_at = null
                """, INSTRUCTOR_USER_ID, OPERATOR_EMAIL, hash);

        jdbc.batchUpdate("""
                insert into users
                  (user_id, role, email, password, name, nickname, provider, email_verified, is_locked,
                   created_at, updated_at)
                values
                  (?, 'STUDENT', ?, ?, ?, ?, 'LOCAL', true, false, now(6), now(6))
                on duplicate key update
                  role = values(role), password = values(password), name = values(name),
                  nickname = values(nickname), updated_at = now(6), deleted_at = null
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                long userId = STUDENT_ID_START + i;
                ps.setLong(1, userId);
                ps.setString(2, "learning_student_%03d@test.com".formatted(i + 1));
                ps.setString(3, hash);
                ps.setString(4, "러닝학생%03d".formatted(i + 1));
                ps.setString(5, "learning-student-%03d".formatted(i + 1));
            }

            @Override
            public int getBatchSize() {
                return STUDENTS;
            }
        });
    }

    private void seedTrustedDevices() {
        String deviceFp = sha256(LOADTEST_DEVICE_ID);

        seedTrustedDevice(ADMIN_USER_ID, deviceFp, "learning-loadtest-k6");
        seedTrustedDevice(INSTRUCTOR_USER_ID, deviceFp, "learning-loadtest-k6");
    }

    private void seedTrustedDevice(Long userId, String deviceFp, String deviceName) {
        int updated = jdbc.update("""
                update trusted_devices
                set device_name = ?,
                    last_country = null,
                    last_city = null,
                    last_used_at = now(6)
                where user_id = ?
                  and device_fp = ?
                """, deviceName, userId, deviceFp);

        if (updated > 0) {
            return;
        }

        jdbc.update("""
                insert into trusted_devices
                  (user_id, device_fp, device_name, last_country, last_city, last_used_at, created_at)
                values
                  (?, ?, ?, null, null, now(6), now(6))
                """, userId, deviceFp, deviceName);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", e);
        }
    }

    private void seedCourse() {
        jdbc.update("""
                insert into course_category
                  (course_category_id, name, status, display_order, created_at)
                values
                  (?, 'Learning Loadtest', 'ACTIVE', 999, now(6))
                on duplicate key update
                  status = values(status)
                """, COURSE_CATEGORY_ID);

        jdbc.update("""
                insert into course
                  (course_id, instructor_id, course_category_id, title, description, thumbnail_url,
                   status, created_at, updated_at, deleted_at)
                values
                  (?, ?, ?, 'Learning Progress Loadtest Course',
                   '학습률 조회 병목 검증용 강좌입니다.',
                   'https://placehold.co/640x360?text=Learning+Loadtest',
                   'ACTIVE', now(6), now(6), null)
                on duplicate key update
                  instructor_id = values(instructor_id), status = values(status), updated_at = now(6), deleted_at = null
                """, COURSE_ID, INSTRUCTOR_USER_ID, COURSE_CATEGORY_ID);
    }

    private void seedLectures() {
        jdbc.batchUpdate("""
                insert into lecture
                  (lecture_id, course_id, title, description, video_url, thumbnail_url, status,
                   lecture_order, created_at, updated_at, deleted_at)
                values
                  (?, ?, ?, '학습률 부하테스트용 강의입니다.', 'https://example.com/video.mp4',
                   'https://placehold.co/320x180', 'ACTIVE', ?, now(6), now(6), null)
                on duplicate key update
                  title = values(title), status = values(status), updated_at = now(6), deleted_at = null
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, LECTURE_ID_START + i);
                ps.setLong(2, COURSE_ID);
                ps.setString(3, "러닝 부하 강의 " + (i + 1));
                ps.setInt(4, i + 1);
            }

            @Override
            public int getBatchSize() {
                return LECTURES;
            }
        });
    }

    private void seedProblemSets() {
        jdbc.batchUpdate("""
                insert into problem_set
                  (problem_set_id, title, description, difficulty, status, total_problem_count,
                   completed_user_count, started_user_count, created_by, created_at, deleted_at)
                values
                  (?, ?, '학습률 부하테스트용 문제세트입니다.', 'EASY', 'ACTIVE', 5, 0, 0, ?, now(6), null)
                on duplicate key update
                  title = values(title), status = values(status), deleted_at = null
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, PROBLEM_SET_ID_START + i);
                ps.setString(2, "러닝 부하 문제세트 " + (i + 1));
                ps.setLong(3, INSTRUCTOR_USER_ID);
            }

            @Override
            public int getBatchSize() {
                return PROBLEM_SETS;
            }
        });
    }

    private void seedLectureProblemSets() {
        jdbc.batchUpdate("""
                insert into lecture_problem_set
                  (lecture_problem_set_id, course_id, lecture_id, problem_set_id, role, display_order)
                values
                  (?, ?, ?, ?, 'MAIN', ?)
                on duplicate key update
                  role = values(role), display_order = values(display_order)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, LECTURE_PROBLEM_SET_ID_START + i);
                ps.setLong(2, COURSE_ID);
                ps.setLong(3, LECTURE_ID_START + (i % LECTURES));
                ps.setLong(4, PROBLEM_SET_ID_START + i);
                ps.setInt(5, i + 1);
            }

            @Override
            public int getBatchSize() {
                return PROBLEM_SETS;
            }
        });
    }

    private void seedEnrollments() {
        jdbc.batchUpdate("""
                insert into enrollment
                  (user_id, course_id, status, enrolled_at, canceled_at)
                values
                  (?, ?, 'ACTIVE', now(6), null)
                on duplicate key update
                  status = values(status), canceled_at = null
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, STUDENT_ID_START + i);
                ps.setLong(2, COURSE_ID);
            }

            @Override
            public int getBatchSize() {
                return STUDENTS;
            }
        });
    }

    private void seedLectureProgresses() {
        jdbc.update("""
                delete from lecture_progress
                where user_id >= ?
                  and user_id < ?
                  and lecture_id >= ?
                  and lecture_id < ?
                """, STUDENT_ID_START, STUDENT_ID_START + STUDENTS, LECTURE_ID_START, LECTURE_ID_START + LECTURES);

        jdbc.batchUpdate("""
                insert into lecture_progress
                  (user_id, lecture_id, is_completed, completed_at, last_position_sec,
                   duration_sec, watched_sec, created_at, updated_at)
                values
                  (?, ?, ?, if(?, now(6), null), 0, 600, 600, now(6), now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                long studentId = STUDENT_ID_START + (i / LECTURES);
                long lectureId = LECTURE_ID_START + (i % LECTURES);
                boolean completed = i % 3 != 0;

                ps.setLong(1, studentId);
                ps.setLong(2, lectureId);
                ps.setBoolean(3, completed);
                ps.setBoolean(4, completed);
            }

            @Override
            public int getBatchSize() {
                return STUDENTS * LECTURES;
            }
        });
    }

    private void seedLectureProblemProgresses() {
        jdbc.update("""
                delete from lecture_problem_progress
                where user_id >= ?
                  and user_id < ?
                  and lecture_problem_set_id >= ?
                  and lecture_problem_set_id < ?
                """, STUDENT_ID_START, STUDENT_ID_START + STUDENTS,
                LECTURE_PROBLEM_SET_ID_START, LECTURE_PROBLEM_SET_ID_START + PROBLEM_SETS);

        jdbc.batchUpdate("""
                insert into lecture_problem_progress
                  (user_id, lecture_problem_set_id, current_problem_number, is_completed,
                   completed_at, created_at, updated_at)
                values
                  (?, ?, 5, ?, if(?, now(6), null), now(6), now(6))
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                long studentId = STUDENT_ID_START + (i / PROBLEM_SETS);
                long lectureProblemSetId = LECTURE_PROBLEM_SET_ID_START + (i % PROBLEM_SETS);
                boolean completed = i % 4 != 0;

                ps.setLong(1, studentId);
                ps.setLong(2, lectureProblemSetId);
                ps.setBoolean(3, completed);
                ps.setBoolean(4, completed);
            }

            @Override
            public int getBatchSize() {
                return STUDENTS * PROBLEM_SETS;
            }
        });
    }
}
