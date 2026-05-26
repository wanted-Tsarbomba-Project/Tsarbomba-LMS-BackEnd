package com.wanted.codebombalms.admin.operation.alert.presentation.api.response;

import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertAssigneeInfo;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertDetail;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertMetricInfo;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertRuleInfo;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertTargetInfo;
import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
// 운영 알림 상세 조회 결과를 API 응답 형태로 변환한다.
public class OperationAlertDetailResponse {

    private AlertInfo alert;
    private RuleInfo rule;
    private TargetInfo target;
    private AssigneeInfo assignee;
    private MetricInfo metric;

    // application 상세 조회 결과를 API 응답 DTO로 변환한다.
    public static OperationAlertDetailResponse from(OperationAlertDetail detail) {
        return new OperationAlertDetailResponse(
                AlertInfo.from(detail),
                RuleInfo.from(detail.rule()),
                TargetInfo.from(detail.target()),
                AssigneeInfo.from(detail.assignee()),
                MetricInfo.from(detail.metric())
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    // 알림 엔티티의 기본 상세 정보를 응답에 담는다.
    public static class AlertInfo {

        private Long operationAlertId;
        private Long operationRuleId;
        private OperationTargetType targetType;
        private Long targetId;
        private BigDecimal detectedValue;
        private BigDecimal thresholdValueSnapshot;
        private OperationSeverity severity;
        private OperationAlertStatus status;
        private Long assigneeId;
        private String reason;
        private String recommendedAction;
        private LocalDateTime firstDetectedAt;
        private LocalDateTime lastDetectedAt;
        private Long resolvedBy;
        private LocalDateTime resolvedAt;
        private String adminMemo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // application 상세 조회 결과에서 알림 기본 정보만 추출한다.
        public static AlertInfo from(OperationAlertDetail detail) {
            return new AlertInfo(
                    detail.operationAlertId(),
                    detail.operationRuleId(),
                    detail.targetType(),
                    detail.targetId(),
                    detail.detectedValue(),
                    detail.thresholdValueSnapshot(),
                    detail.severity(),
                    detail.status(),
                    detail.assigneeId(),
                    detail.reason(),
                    detail.recommendedAction(),
                    detail.firstDetectedAt(),
                    detail.lastDetectedAt(),
                    detail.resolvedBy(),
                    detail.resolvedAt(),
                    detail.adminMemo(),
                    detail.createdAt(),
                    detail.updatedAt()
            );
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    // 알림을 발생시킨 자동 규칙 정보를 응답에 담는다.
    public static class RuleInfo {

        private OperationRuleCode ruleCode;
        private String ruleName;
        private String description;
        private String thresholdLabel;
        private String thresholdUnit;
        private Integer minSampleCount;
        private String minSampleCountLabel;
        private String minSampleCountUnit;

        // application 규칙 정보를 API 응답 DTO로 변환한다.
        public static RuleInfo from(OperationAlertRuleInfo rule) {
            return new RuleInfo(
                    rule.ruleCode(),
                    rule.ruleName(),
                    rule.description(),
                    rule.thresholdLabel(),
                    rule.thresholdUnit(),
                    rule.minSampleCount(),
                    rule.minSampleCountLabel(),
                    rule.minSampleCountUnit()
            );
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    // 알림 대상인 강좌, 문제 또는 사용자 정보를 응답에 담는다.
    public static class TargetInfo {

        private OperationTargetType targetType;
        private Long targetId;
        private String title;
        private String status;
        private String nickname;
        private String email;
        private Long courseId;
        private String courseTitle;
        private Long problemSetId;
        private String problemSetTitle;

        // application 대상 정보를 API 응답 DTO로 변환한다.
        public static TargetInfo from(OperationAlertTargetInfo target) {
            return new TargetInfo(
                    target.targetType(),
                    target.targetId(),
                    target.title(),
                    target.status(),
                    target.nickname(),
                    target.email(),
                    target.courseId(),
                    target.courseTitle(),
                    target.problemSetId(),
                    target.problemSetTitle()
            );
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    // 알림 대상의 담당자 정보를 응답에 담는다.
    public static class AssigneeInfo {

        private Long userId;
        private String name;
        private String nickname;
        private String email;
        private String role;

        // application 담당자 정보를 API 응답 DTO로 변환한다.
        public static AssigneeInfo from(OperationAlertAssigneeInfo assignee) {
            if (assignee == null) {
                return null;
            }

            return new AssigneeInfo(
                    assignee.userId(),
                    assignee.name(),
                    assignee.nickname(),
                    assignee.email(),
                    assignee.role()
            );
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    // 알림 상세 화면에 표시할 관측값과 기준값 정보를 응답에 담는다.
    public static class MetricInfo {

        private String observedLabel;
        private BigDecimal observedValue;
        private String thresholdLabel;
        private BigDecimal thresholdValue;
        private String unit;
        private Integer minSampleCount;
        private String minSampleCountUnit;

        // application 지표 정보를 API 응답 DTO로 변환한다.
        public static MetricInfo from(OperationAlertMetricInfo metric) {
            return new MetricInfo(
                    metric.observedLabel(),
                    metric.observedValue(),
                    metric.thresholdLabel(),
                    metric.thresholdValue(),
                    metric.unit(),
                    metric.minSampleCount(),
                    metric.minSampleCountUnit()
            );
        }
    }
}
