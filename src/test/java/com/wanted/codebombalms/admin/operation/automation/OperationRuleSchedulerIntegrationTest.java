package com.wanted.codebombalms.admin.operation.automation;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.alert.application.port.OperationAlertTargetDetailPort;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertRuleInfo;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertTargetDetail;
import com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence.OperationAlertJpaEntity;
import com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence.SpringDataOperationAlertRepository;
import com.wanted.codebombalms.admin.operation.automation.application.usecase.RunOperationRuleUseCase;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import com.wanted.codebombalms.admin.operation.rule.domain.repository.AutomationRuleRepository;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseJpaEntity;
import com.wanted.codebombalms.problems.category.infrastructure.persistence.ProblemCategoryJpaEntity;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import com.wanted.codebombalms.submission.infrastructure.persistence.SubmissionJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("운영 자동 알림 스케줄러 통합 테스트")
// 운영 자동 규칙 실행 전후의 알림 생성 상태 변화를 검증한다.
class OperationRuleSchedulerIntegrationTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @Autowired
    private AutomationRuleRepository automationRuleRepository;

    @Autowired
    private SpringDataOperationAlertRepository operationAlertRepository;

    @Autowired
    private RunOperationRuleUseCase runOperationRuleUseCase;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OperationAlertTargetDetailPort operationAlertTargetDetailPort;

    @Autowired
    private MutableClock mutableClock;

    @Test
    @DisplayName("08시 59분에는 알림이 없고 09시 규칙 실행 후 강좌/학생/문제 알림이 생성된다.")
    void operationRuleSchedulerCreatesAlertsAtNine() {
        // given
        LocalDateTime beforeScheduleTime = LocalDateTime.of(2026, 5, 24, 8, 59);
        LocalDateTime scheduleTime = beforeScheduleTime.plusMinutes(1);
        mutableClock.setTime(beforeScheduleTime);

        seedRules();
        seedCourseForLowEnrollmentAlert();
        CourseJpaEntity enrolledCourse = seedCourseForEnrollment();
        seedInactiveStudentWithActiveEnrollment(enrolledCourse.getCourseId());
        seedProblemForHighWrongRateAlert();
        entityManager.flush();
        entityManager.clear();

        // 1. 등록된 규칙 조회
        List<AutomationRule> registeredRules = automationRuleRepository.findAllActive(null);
        printRules(registeredRules);
        assertEquals(3, registeredRules.size());
        assertTrue(registeredRules.stream()
                .anyMatch(rule -> rule.getRuleCode() == OperationRuleCode.COURSE_LOW_ENROLLMENT));
        assertTrue(registeredRules.stream()
                .anyMatch(rule -> rule.getRuleCode() == OperationRuleCode.USER_INACTIVE_NO_COURSE));
        assertTrue(registeredRules.stream()
                .anyMatch(rule -> rule.getRuleCode() == OperationRuleCode.PROBLEM_HIGH_WRONG_RATE));

        // 2. 현재 시간이 08시 59분이라고 가정하고 알람 조회
        List<OperationAlertJpaEntity> alertsAtBeforeScheduleTime = operationAlertRepository.findAll();
        printAlerts("08:59 before scheduler", alertsAtBeforeScheduleTime);
        assertEquals(0, alertsAtBeforeScheduleTime.size());

        // 3. 1분 뒤에 스케줄러가 실행하는 유스케이스를 호출하고 알람 조회
        mutableClock.setTime(scheduleTime);
        runOperationRuleUseCase.run();
        entityManager.flush();
        entityManager.clear();

        List<OperationAlertJpaEntity> alertsAfterScheduleTime = operationAlertRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(OperationAlertJpaEntity::getTargetType))
                .toList();
        printAlerts("09:00 after scheduler", alertsAfterScheduleTime);

        assertEquals(3, alertsAfterScheduleTime.size());
        assertCreatedAlert(
                findAlert(alertsAfterScheduleTime, OperationTargetType.COURSE),
                OperationTargetType.COURSE,
                BigDecimal.ZERO,
                scheduleTime
        );
        OperationAlertJpaEntity userAlert = findAlert(alertsAfterScheduleTime, OperationTargetType.USER);
        assertCreatedAlert(
                userAlert,
                OperationTargetType.USER,
                BigDecimal.valueOf(20),
                scheduleTime
        );
        assertCreatedAlert(
                findAlert(alertsAfterScheduleTime, OperationTargetType.PROBLEM),
                OperationTargetType.PROBLEM,
                BigDecimal.valueOf(66.67),
                scheduleTime
        );
        assertUserTargetDetail(userAlert);
    }

    private void printRules(List<AutomationRule> rules) {
        System.out.println();
        System.out.println("========== registered operation rules ==========");
        System.out.println("rule count = " + rules.size());
        rules.forEach(rule -> System.out.println(
                "ruleId=" + rule.getOperationRuleId()
                        + ", ruleCode=" + rule.getRuleCode()
                        + ", targetType=" + rule.getTargetType()
                        + ", threshold=" + rule.getThresholdValue()
                        + ", minSampleCount=" + rule.getMinSampleCount()
                        + ", severity=" + rule.getSeverity()
                        + ", enabled=" + rule.isEnabled()
        ));
        System.out.println("================================================");
        System.out.println();
    }

    private void printAlerts(String label, List<OperationAlertJpaEntity> alerts) {
        System.out.println();
        System.out.println("========== " + label + " ==========");
        System.out.println("alert count = " + alerts.size());
        alerts.forEach(alert -> System.out.println(
                "alertId=" + alert.getOperationAlertId()
                        + ", ruleId=" + alert.getOperationRuleId()
                        + ", targetType=" + alert.getTargetType()
                        + ", targetId=" + alert.getTargetId()
                        + ", detectedValue=" + alert.getDetectedValue()
                        + ", thresholdSnapshot=" + alert.getThresholdValueSnapshot()
                        + ", status=" + alert.getStatus()
                        + ", firstDetectedAt=" + alert.getFirstDetectedAt()
                        + ", lastDetectedAt=" + alert.getLastDetectedAt()
                        + ", reason=" + alert.getReason()
                        + ", recommendedAction=" + alert.getRecommendedAction()
        ));
        System.out.println("========================================");
        System.out.println();
    }

    private void seedRules() {
        automationRuleRepository.save(AutomationRule.create(
                1L,
                OperationRuleCode.COURSE_LOW_ENROLLMENT,
                BigDecimal.ZERO,
                null,
                OperationSeverity.MEDIUM,
                true
        ));
        automationRuleRepository.save(AutomationRule.create(
                1L,
                OperationRuleCode.PROBLEM_HIGH_WRONG_RATE,
                BigDecimal.valueOf(50),
                3,
                OperationSeverity.HIGH,
                true
        ));
        automationRuleRepository.save(AutomationRule.create(
                1L,
                OperationRuleCode.USER_INACTIVE_NO_COURSE,
                BigDecimal.valueOf(10),
                null,
                OperationSeverity.MEDIUM,
                true
        ));
    }

    private CourseJpaEntity seedCourseForLowEnrollmentAlert() {
        CourseJpaEntity course = new CourseJpaEntity(
                10L,
                "수강생이 없는 운영 점검 강좌",
                "운영 자동 알림 테스트용 강좌입니다.",
                "course.png",
                CourseStatus.ACTIVE
        );
        entityManager.persist(course);
        entityManager.flush();

        return course;
    }

    private CourseJpaEntity seedCourseForEnrollment() {
        CourseJpaEntity course = new CourseJpaEntity(
                10L,
                "학생이 수강 중인 강좌",
                "미수강 조건 제거 검증용 강좌입니다.",
                "enrolled-course.png",
                CourseStatus.ACTIVE
        );
        entityManager.persist(course);
        entityManager.flush();

        return course;
    }

    private void seedInactiveStudentWithActiveEnrollment(Long courseId) {
        entityManager.createNativeQuery("""
                        insert into users (
                            user_id,
                            role,
                            email,
                            password,
                            name,
                            nickname,
                            provider,
                            email_verified,
                            is_locked,
                            created_at,
                            updated_at
                        )
                        values (
                            100,
                            'STUDENT',
                            'inactive-student@test.com',
                            'encoded',
                            '장기미접속 학생',
                            'inactive',
                            'LOCAL',
                            true,
                            false,
                            '2026-05-01 00:00:00',
                            '2026-05-01 00:00:00'
                        )
                        """)
                .executeUpdate();
        entityManager.createNativeQuery("""
                        insert into login_history (
                            user_id,
                            ip_address,
                            user_agent,
                            device_fp,
                            country,
                            city,
                            is_suspicious,
                            created_at
                        )
                        values (
                            100,
                            '127.0.0.1',
                            'test-agent',
                            'device',
                            'KR',
                            'Seoul',
                            false,
                            '2026-05-04 09:00:00'
                        )
                        """)
                .executeUpdate();
        entityManager.createNativeQuery("""
                        insert into enrollment (
                            user_id,
                            course_id,
                            status,
                            enrolled_at
                        )
                        values (
                            100,
                            :courseId,
                            'ACTIVE',
                            '2026-05-05 09:00:00'
                        )
                        """)
                .setParameter("courseId", courseId)
                .executeUpdate();
    }

    private void seedProblemForHighWrongRateAlert() {
        ProblemCategoryJpaEntity category = new ProblemCategoryJpaEntity("Java", "Java 문제");
        entityManager.persist(category);

        ProblemSetJpaEntity problemSet = new ProblemSetJpaEntity(
                category,
                "오답률 점검 문제 세트",
                "운영 자동 알림 테스트용 문제 세트입니다.",
                "EASY",
                1,
                1L
        );
        entityManager.persist(problemSet);

        ProblemJpaEntity problem = new ProblemJpaEntity(
                problemSet,
                "오답률 높은 문제",
                "1 + 1은?",
                "2",
                "기본 덧셈 문제입니다.",
                10,
                1
        );
        entityManager.persist(problem);

        entityManager.persist(createSubmission(1L, problem, false, 1));
        entityManager.persist(createSubmission(2L, problem, false, 1));
        entityManager.persist(createSubmission(3L, problem, true, 1));
    }

    private SubmissionJpaEntity createSubmission(
            Long userId,
            ProblemJpaEntity problem,
            Boolean correct,
            Integer attemptNo
    ) {
        return new SubmissionJpaEntity(
                userId,
                problem,
                "answer",
                correct,
                attemptNo
        );
    }

    private void assertCreatedAlert(
            OperationAlertJpaEntity alert,
            OperationTargetType targetType,
            BigDecimal detectedValue,
            LocalDateTime detectedAt
    ) {
        assertEquals(targetType, alert.getTargetType());
        assertEquals(OperationAlertStatus.OPEN, alert.getStatus());
        assertEquals(0, detectedValue.compareTo(alert.getDetectedValue()));
        assertEquals(detectedAt, alert.getFirstDetectedAt());
        assertEquals(detectedAt, alert.getLastDetectedAt());
    }

    private OperationAlertJpaEntity findAlert(
            List<OperationAlertJpaEntity> alerts,
            OperationTargetType targetType
    ) {
        return alerts.stream()
                .filter(alert -> alert.getTargetType() == targetType)
                .findFirst()
                .orElseThrow();
    }

    private void assertUserTargetDetail(OperationAlertJpaEntity userAlert) {
        OperationAlertTargetDetail targetDetail = operationAlertTargetDetailPort.loadTargetDetail(
                userAlert.getTargetType(),
                userAlert.getTargetId(),
                userAlert.getDetectedValue(),
                userAlert.getThresholdValueSnapshot(),
                OperationAlertRuleInfo.from(OperationRuleCode.USER_INACTIVE_NO_COURSE, null)
        );

        assertEquals("장기미접속 학생", targetDetail.target().title());
        assertEquals("inactive", targetDetail.target().nickname());
        assertEquals("inactive-student@test.com", targetDetail.target().email());
    }

    @TestConfiguration
    static class TestClockConfig {

        @Bean
        @Primary
        MutableClock mutableClock() {
            return new MutableClock(
                    LocalDateTime.of(2026, 5, 24, 8, 59)
                            .atZone(SEOUL_ZONE)
                            .toInstant(),
                    SEOUL_ZONE
            );
        }
    }

    static class MutableClock extends Clock {

        private Instant instant;
        private final ZoneId zone;

        MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void setTime(LocalDateTime time) {
            this.instant = time.atZone(zone).toInstant();
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
