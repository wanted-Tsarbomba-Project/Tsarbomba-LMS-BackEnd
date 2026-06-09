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
@DisplayName("Operation rule scheduler integration test")
// ??怨멸껀 ???吏??잙?裕?????덈뺄 ?熬곥굦??????逾???諛댁뎽 ??⑤객臾??곌떠???? ?롪틵?嶺뚯빘鍮쒒뇡??
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
    @DisplayName("08??59?釉뚯뫒??????逾?????㈑?09???잙?裕?????덈뺄 ???띠룆踰▽펺????뉖Ц/??쒖굣?????逾????諛댁뎽??類ｋ펲.")
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

        // 1. ?繹먮굞夷???잙?裕???브퀗???
        List<AutomationRule> registeredRules = automationRuleRepository.findAllActive(null);
        printRules(registeredRules);
        assertEquals(3, registeredRules.size());
        assertTrue(registeredRules.stream()
                .anyMatch(rule -> rule.getRuleCode() == OperationRuleCode.COURSE_LOW_ENROLLMENT));
        assertTrue(registeredRules.stream()
                .anyMatch(rule -> rule.getRuleCode() == OperationRuleCode.USER_INACTIVE_NO_COURSE));
        assertTrue(registeredRules.stream()
                .anyMatch(rule -> rule.getRuleCode() == OperationRuleCode.PROBLEM_HIGH_WRONG_RATE));

        // 2. ?熬곣뫗????蹂?뜟??08??59?釉뚯뫒????┑??띠럾??筌먐삳┃?????肉??브퀗???
        List<OperationAlertJpaEntity> alertsAtBeforeScheduleTime = operationAlertRepository.findAll();
        printAlerts("08:59 before scheduler", alertsAtBeforeScheduleTime);
        assertEquals(0, alertsAtBeforeScheduleTime.size());

        // 3. 1?????고뱺 ???繞벿븐뫊??泥? ???덈뺄??濡ル츎 ??ル∥裕??댟??怨룸츩???筌뤾쑵????겶????肉??브퀗???
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
                OperationRuleCode.COURSE_LOW_ENROLLMENT,
                BigDecimal.ZERO,
                null,
                OperationSeverity.MEDIUM,
                true
        ));
        automationRuleRepository.save(AutomationRule.create(
                OperationRuleCode.PROBLEM_HIGH_WRONG_RATE,
                BigDecimal.valueOf(50),
                3,
                OperationSeverity.HIGH,
                true
        ));
        automationRuleRepository.save(AutomationRule.create(
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
                "Low enrollment course",
                "Course for low enrollment alert",
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
                "Enrolled course",
                "Course for inactive user enrollment",
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
                            '?鰲?깃퀎泥뗰쭗袁⑹젘?????뉖Ц',
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
        ProblemCategoryJpaEntity category = new ProblemCategoryJpaEntity("Java", "Java problems");
        entityManager.persist(category);

        ProblemSetJpaEntity problemSet = new ProblemSetJpaEntity(
                category,
                "High wrong rate problem set",
                "Problem set for operation alert test",
                "EASY",
                1,
                1L
        );
        entityManager.persist(problemSet);

        ProblemJpaEntity problem = new ProblemJpaEntity(
                problemSet,
                "High wrong rate problem",
                "What is 1 + 1?",
                "TEXT",
                "EASY",
                "2",
                "Basic arithmetic problem",
                10,
                10,
                true,
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

        assertEquals("?鰲?깃퀎泥뗰쭗袁⑹젘?????뉖Ц", targetDetail.target().title());
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
