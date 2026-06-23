package com.wanted.codebombalms.admin.operation.automation.infrastructure.adapter;

import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.automation.application.port.UserOperationMetricPort;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.auth.application.usecase.LoginActivityQueryUseCase;
import com.wanted.codebombalms.user.application.usecase.UserOperationQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 사용자와 로그인 이력을 조회해 장기 미접속 학생 지표를 만든다.
public class UserOperationMetricAdapter implements UserOperationMetricPort {

    private final UserOperationQueryUseCase userOperationQueryUseCase;
    private final LoginActivityQueryUseCase loginActivityQueryUseCase;
    private final Clock clock;

    @Override
    public List<OperationRuleDetectionResult> findInactiveUsers(BigDecimal inactiveDaysThreshold) {
        long inactiveDays = inactiveDaysThreshold.longValue();
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime cutoff = now.minusDays(inactiveDays);
        List<UserOperationQueryUseCase.UserOperationView> students = userOperationQueryUseCase.findStudents();
        List<Long> userIds = students.stream()
                .map(UserOperationQueryUseCase.UserOperationView::userId)
                .toList();
        Map<Long, LocalDateTime> latestLoginAtByUserId = loginActivityQueryUseCase.findLatestLoginAtByUserIds(userIds);

        return students.stream()
                .map(student -> toMetric(student, now, latestLoginAtByUserId))
                .filter(metric -> !metric.lastActivityAt().isAfter(cutoff))
                .map(metric -> toResult(metric, inactiveDaysThreshold))
                .toList();
    }

    private UserInactiveMetric toMetric(
            UserOperationQueryUseCase.UserOperationView student,
            LocalDateTime now,
            Map<Long, LocalDateTime> latestLoginAtByUserId
    ) {
        LocalDateTime lastActivityAt = latestLoginAtByUserId.getOrDefault(student.userId(), student.createdAt());
        long inactiveDays = ChronoUnit.DAYS.between(lastActivityAt, now);

        return new UserInactiveMetric(
                student.userId(),
                student.name(),
                student.email(),
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
