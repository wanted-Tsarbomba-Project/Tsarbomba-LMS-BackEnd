package com.wanted.codebombalms.admin.operation.automation.infrastructure.adapter;

import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.automation.application.port.UserOperationMetricPort;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.user.domain.model.UserRole;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 사용자와 로그인 이력을 조회해 장기 미접속 학생 지표를 만든다.
public class UserOperationMetricAdapter implements UserOperationMetricPort {

    private final EntityManager entityManager;
    private final Clock clock;

    @Override
    public List<OperationRuleDetectionResult> findInactiveUsers(BigDecimal inactiveDaysThreshold) {
        long inactiveDays = inactiveDaysThreshold.longValue();
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime cutoff = now.minusDays(inactiveDays);

        return entityManager.createQuery("""
                        select u.userId,
                               u.name,
                               u.email,
                               coalesce(max(lh.createdAt), u.createdAt)
                        from UserJpaEntity u
                        left join LoginHistoryJpaEntity lh
                            on lh.userId = u.userId
                        where u.deletedAt is null
                          and u.role = :studentRole
                        group by u.userId, u.name, u.email, u.createdAt
                        """, Object[].class)
                .setParameter("studentRole", UserRole.STUDENT)
                .getResultList()
                .stream()
                .map(row -> toMetric(row, now))
                .filter(metric -> !metric.lastActivityAt().isAfter(cutoff))
                .map(metric -> toResult(metric, inactiveDaysThreshold))
                .toList();
    }

    private UserInactiveMetric toMetric(Object[] row, LocalDateTime now) {
        LocalDateTime lastActivityAt = (LocalDateTime) row[3];
        long inactiveDays = ChronoUnit.DAYS.between(lastActivityAt, now);

        return new UserInactiveMetric(
                (Long) row[0],
                (String) row[1],
                (String) row[2],
                lastActivityAt,
                inactiveDays
        );
    }

    private OperationRuleDetectionResult toResult(
            UserInactiveMetric metric,
            BigDecimal inactiveDaysThreshold
    ) {
        return new OperationRuleDetectionResult(
                OperationTargetType.USER,
                metric.userId(),
                BigDecimal.valueOf(metric.inactiveDays()),
                "마지막 활동 후 기준 일수 이상 지났습니다. 현재 미로그인 기간: " + metric.inactiveDays() + "일",
                "학생에게 학습 복귀 안내를 발송하세요. 이메일: " + metric.email()
                        + ", 기준값: " + inactiveDaysThreshold + "일"
        );
    }

    private record UserInactiveMetric(
            Long userId,
            String name,
            String email,
            LocalDateTime lastActivityAt,
            long inactiveDays
    ) {
    }
}
