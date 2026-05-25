package com.wanted.codebombalms.admin.operation.alert;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence.OperationAlertJpaEntity;
import com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence.SpringDataOperationAlertRepository;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.global.application.cleanup.HardDeleteExecutor;
import com.wanted.codebombalms.global.application.cleanup.port.HardDeleteTarget;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("운영 알림 하드 딜리트 통합 테스트")
// deleted_at 기준 6개월 보존 정책으로 소프트 딜리트된 운영 알림이 하드 딜리트되는지 검증한다.
class OperationAlertHardDeleteIntegrationTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 24, 3, 0);

    @Autowired
    private HardDeleteExecutor hardDeleteExecutor;

    @Autowired
    @Qualifier("operationAlertHardDeleteTarget")
    private HardDeleteTarget operationAlertHardDeleteTarget;

    @Autowired
    private SpringDataOperationAlertRepository operationAlertRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("deleted_at이 6개월 이전인 운영 알림만 하드 딜리트한다.")
    void hardDeleteOperationAlertsByDeletedAtBeforeSixMonths() {
        // given
        OperationAlertJpaEntity oldDeletedAlert = createAlert(
                1L,
                OperationAlertStatus.RESOLVED,
                NOW.minusMonths(8),
                NOW.minusMonths(6).minusDays(1)
        );
        OperationAlertJpaEntity recentDeletedAlert = createAlert(
                2L,
                OperationAlertStatus.RESOLVED,
                NOW.minusMonths(8),
                NOW.minusMonths(6).plusDays(1)
        );
        OperationAlertJpaEntity resolvedNotDeletedAlert = createAlert(
                3L,
                OperationAlertStatus.RESOLVED,
                NOW.minusMonths(8),
                null
        );
        OperationAlertJpaEntity openAlert = createAlert(
                4L,
                OperationAlertStatus.OPEN,
                null,
                null
        );

        entityManager.persist(oldDeletedAlert);
        entityManager.persist(recentDeletedAlert);
        entityManager.persist(resolvedNotDeletedAlert);
        entityManager.persist(openAlert);
        entityManager.flush();
        entityManager.clear();

        // when
        int deletedCount = hardDeleteExecutor.execute(operationAlertHardDeleteTarget);
        entityManager.flush();
        entityManager.clear();

        // then
        List<OperationAlertJpaEntity> remainingAlerts = operationAlertRepository.findAll();

        assertEquals(1, deletedCount);
        assertEquals(3, remainingAlerts.size());
        assertTrue(remainingAlerts.stream()
                .noneMatch(alert -> alert.getTargetId().equals(oldDeletedAlert.getTargetId())));
        assertTrue(remainingAlerts.stream()
                .anyMatch(alert -> alert.getTargetId().equals(recentDeletedAlert.getTargetId())));
        assertTrue(remainingAlerts.stream()
                .anyMatch(alert -> alert.getTargetId().equals(resolvedNotDeletedAlert.getTargetId())));
        assertTrue(remainingAlerts.stream()
                .anyMatch(alert -> alert.getTargetId().equals(openAlert.getTargetId())));
    }

    private OperationAlertJpaEntity createAlert(
            Long targetId,
            OperationAlertStatus status,
            LocalDateTime resolvedAt,
            LocalDateTime deletedAt
    ) {
        return new OperationAlertJpaEntity(
                null,
                1L,
                OperationTargetType.COURSE,
                targetId,
                BigDecimal.ONE,
                BigDecimal.TEN,
                1L,
                "테스트 운영 알림입니다.",
                "테스트 조치가 필요합니다.",
                NOW.minusMonths(8),
                NOW.minusMonths(8),
                status,
                resolvedAt == null ? null : 1L,
                resolvedAt,
                null,
                NOW.minusMonths(8),
                NOW.minusMonths(8),
                deletedAt
        );
    }

    @TestConfiguration
    static class TestClockConfig {

        @Bean
        @Primary
        Clock cleanupTestClock() {
            return Clock.fixed(
                    NOW.atZone(SEOUL_ZONE).toInstant(),
                    SEOUL_ZONE
            );
        }
    }
}
