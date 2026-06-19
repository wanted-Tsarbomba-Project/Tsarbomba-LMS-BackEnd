package com.wanted.codebombalms.admin.operation.alert.infrastructure.adapter;

import com.wanted.codebombalms.admin.operation.alert.application.port.OperationAlertTargetDetailPort;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertAssigneeInfo;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertMetricInfo;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertRuleInfo;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertTargetDetail;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertTargetInfo;
import com.wanted.codebombalms.admin.operation.alert.domain.exception.OperationAlertErrorCode;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.metrics.AdminMetrics;
import com.wanted.codebombalms.course.application.usecase.CourseQueryUseCase;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.problem.application.port.ProblemTargetDetailPort.ProblemTargetDetailView;
import com.wanted.codebombalms.problems.problem.application.usecase.ProblemTargetDetailQueryUseCase;
import com.wanted.codebombalms.user.application.query.StudentDetail;
import com.wanted.codebombalms.user.application.usecase.GetStudentDetailUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
// COURSE, PROBLEM, USER 알림 대상의 상세 정보와 담당자 정보를 조회한다.
public class OperationAlertTargetDetailAdapter implements OperationAlertTargetDetailPort {

    private final CourseQueryUseCase courseQueryUseCase;
    private final ProblemTargetDetailQueryUseCase problemTargetDetailQueryUseCase;
    private final GetStudentDetailUseCase getStudentDetailUseCase;
    private final AdminMetrics adminMetrics;

    @Override
    // 대상 타입에 따라 강좌, 문제 또는 사용자 상세 조회로 분기한다.
    public OperationAlertTargetDetail loadTargetDetail(
            OperationTargetType targetType,
            Long targetId,
            BigDecimal detectedValue,
            BigDecimal thresholdValue,
            OperationAlertRuleInfo rule
    ) {
        long startedAt = System.nanoTime();
        OperationAlertTargetDetail result = switch (targetType) {
            case COURSE -> loadCourseDetail(targetId, detectedValue, thresholdValue, rule);
            case PROBLEM -> loadProblemDetail(targetId, detectedValue, thresholdValue, rule);
            case USER -> loadUserDetail(targetId, detectedValue, thresholdValue, rule);
        };
        long elapsedNanos = System.nanoTime() - startedAt;

        adminMetrics.recordTargetDetail(targetType, elapsedNanos);
        log.info("event=admin_operation_alert_target_detail_loaded targetType={} durationMs={}",
                targetType, elapsedNanos / 1_000_000);

        return result;
    }

    // 강좌 알림의 강좌 정보와 강좌 담당자 정보를 조회한다.
    private OperationAlertTargetDetail loadCourseDetail(
            Long courseId,
            BigDecimal detectedValue,
            BigDecimal thresholdValue,
            OperationAlertRuleInfo rule
    ) {
        Course course = courseQueryUseCase.findCourseByIdForOperator(courseId);

        OperationAlertTargetInfo target = new OperationAlertTargetInfo(
                OperationTargetType.COURSE,
                course.getCourseId(),
                course.getTitle(),
                toStringOrNull(course.getStatus()),
                null,
                null,
                course.getCourseId(),
                course.getTitle(),
                null,
                null
        );

        return new OperationAlertTargetDetail(
                target,
                loadAssignee(course.getInstructorId()),
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
        ProblemTargetDetailView problem = problemTargetDetailQueryUseCase.findProblemTargetDetail(problemId);

        OperationAlertTargetInfo target = new OperationAlertTargetInfo(
                OperationTargetType.PROBLEM,
                problem.problemId(),
                problem.title(),
                problem.status(),
                null,
                null,
                null,
                null,
                problem.problemSetId(),
                problem.problemSetTitle()
        );

        return new OperationAlertTargetDetail(
                target,
                loadAssignee(problem.createdBy()),
                toMetric(rule, detectedValue, thresholdValue)
        );
    }

    // 사용자 알림의 사용자 연락 정보를 조회한다.
    private OperationAlertTargetDetail loadUserDetail(
            Long userId,
            BigDecimal detectedValue,
            BigDecimal thresholdValue,
            OperationAlertRuleInfo rule
    ) {
        StudentDetail user = loadUser(userId);

        OperationAlertTargetInfo target = new OperationAlertTargetInfo(
                OperationTargetType.USER,
                user.userId(),
                user.name(),
                toUserStatus(user.role(), user.isLocked()),
                user.nickname(),
                user.email(),
                null,
                null,
                null,
                null
        );

        return new OperationAlertTargetDetail(
                target,
                null,
                toMetric(rule, detectedValue, thresholdValue)
        );
    }

    private OperationAlertAssigneeInfo loadAssignee(Long userId) {
        if (userId == null) {
            return null;
        }

        StudentDetail user = loadUser(userId);
        return new OperationAlertAssigneeInfo(
                user.userId(),
                user.name(),
                user.nickname(),
                user.email(),
                toStringOrNull(user.role())
        );
    }

    private StudentDetail loadUser(Long userId) {
        try {
            return getStudentDetailUseCase.getStudentDetail(userId);
        } catch (NotFoundException exception) {
            throw new NotFoundException(OperationAlertErrorCode.OPERATION_ALERT_TARGET_NOT_FOUND);
        }
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

    private String toUserStatus(Object role, Object locked) {
        if (Boolean.TRUE.equals(locked)) {
            return "LOCKED";
        }

        return toStringOrNull(role);
    }
}
