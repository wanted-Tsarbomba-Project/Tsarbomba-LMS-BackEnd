package com.wanted.codebombalms.admin.operation.alert.application.query;

// 운영 알림 대상 상세 조회 결과를 대상, 담당자, 지표 단위로 묶는다.
public record OperationAlertTargetDetail(
        OperationAlertTargetInfo target,
        OperationAlertAssigneeInfo assignee,
        OperationAlertMetricInfo metric
) {
}
