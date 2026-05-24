package com.wanted.codebombalms.admin.operation.alert.infrastructure.adapter;

import com.wanted.codebombalms.admin.operation.alert.application.port.OperationAlertTargetDetailPort;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertAssigneeInfo;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertMetricInfo;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertRuleInfo;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertTargetDetail;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertTargetInfo;
import com.wanted.codebombalms.admin.operation.alert.domain.exception.OperationAlertErrorCode;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
// COURSE와 PROBLEM 알림 대상의 상세 정보와 담당자 정보를 조회한다.
public class OperationAlertTargetDetailAdapter implements OperationAlertTargetDetailPort {

    private final EntityManager entityManager;

    @Override
    // 대상 타입에 따라 강좌 또는 문제 상세 조회로 분기한다.
    public OperationAlertTargetDetail loadTargetDetail(
            OperationTargetType targetType,
            Long targetId,
            BigDecimal detectedValue,
            BigDecimal thresholdValue,
            OperationAlertRuleInfo rule
    ) {
        return switch (targetType) {
            case COURSE -> loadCourseDetail(targetId, detectedValue, thresholdValue, rule);
            case PROBLEM -> loadProblemDetail(targetId, detectedValue, thresholdValue, rule);
            case USER -> throw new ValidationException(OperationAlertErrorCode.UNSUPPORTED_ALERT_TARGET_TYPE);
        };
    }

    // 강좌 알림의 강좌 정보와 강좌 담당자 정보를 조회한다.
    private OperationAlertTargetDetail loadCourseDetail(
            Long courseId,
            BigDecimal detectedValue,
            BigDecimal thresholdValue,
            OperationAlertRuleInfo rule
    ) {
        Object[] row = entityManager.createQuery("""
                        select c.courseId,
                               c.title,
                               c.status,
                               u.userId,
                               u.name,
                               u.nickname,
                               u.email,
                               u.role
                        from CourseJpaEntity c
                        left join UserJpaEntity u
                            on u.userId = c.instructorId
                           and u.deletedAt is null
                        where c.courseId = :courseId
                          and c.deletedAt is null
                        """, Object[].class)
                .setParameter("courseId", courseId)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(OperationAlertErrorCode.OPERATION_ALERT_TARGET_NOT_FOUND));

        OperationAlertTargetInfo target = new OperationAlertTargetInfo(
                OperationTargetType.COURSE,
                (Long) row[0],
                (String) row[1],
                toStringOrNull(row[2]),
                (Long) row[0],
                (String) row[1],
                null,
                null
        );

        return new OperationAlertTargetDetail(
                target,
                toAssignee(row, 3),
                toMetric(rule, detectedValue, thresholdValue)
        );
    }

    // 문제 알림의 문제/문제세트 정보와 문제세트 생성자 정보를 조회한다.
    private OperationAlertTargetDetail loadProblemDetail(
            Long problemId,
            BigDecimal detectedValue,
            BigDecimal thresholdValue,
            OperationAlertRuleInfo rule
    ) {
        Object[] row = entityManager.createQuery("""
                        select p.problemId,
                               p.title,
                               p.status,
                               ps.problemSetId,
                               ps.title,
                               u.userId,
                               u.name,
                               u.nickname,
                               u.email,
                               u.role
                        from ProblemJpaEntity p
                        join p.problemSet ps
                        left join UserJpaEntity u
                            on u.userId = ps.createdBy
                           and u.deletedAt is null
                        where p.problemId = :problemId
                        """, Object[].class)
                .setParameter("problemId", problemId)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(OperationAlertErrorCode.OPERATION_ALERT_TARGET_NOT_FOUND));

        OperationAlertTargetInfo target = new OperationAlertTargetInfo(
                OperationTargetType.PROBLEM,
                (Long) row[0],
                (String) row[1],
                (String) row[2],
                null,
                null,
                (Long) row[3],
                (String) row[4]
        );

        return new OperationAlertTargetDetail(
                target,
                toAssignee(row, 5),
                toMetric(rule, detectedValue, thresholdValue)
        );
    }

    // 조회 결과의 사용자 컬럼을 담당자 정보로 변환한다.
    private OperationAlertAssigneeInfo toAssignee(Object[] row, int startIndex) {
        Long userId = (Long) row[startIndex];
        if (userId == null) {
            return null;
        }

        return new OperationAlertAssigneeInfo(
                userId,
                (String) row[startIndex + 1],
                (String) row[startIndex + 2],
                (String) row[startIndex + 3],
                toStringOrNull(row[startIndex + 4])
        );
    }

    // 규칙 메타데이터와 알림 값을 화면 표시용 지표 정보로 변환한다.
    private OperationAlertMetricInfo toMetric(
            OperationAlertRuleInfo rule,
            BigDecimal detectedValue,
            BigDecimal thresholdValue
    ) {
        return new OperationAlertMetricInfo(
                rule.thresholdLabel(),
                detectedValue,
                rule.thresholdLabel(),
                thresholdValue,
                rule.thresholdUnit(),
                rule.minSampleCount(),
                rule.minSampleCountUnit()
        );
    }

    // enum을 포함한 nullable 값을 응답용 문자열로 변환한다.
    private String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }
}
