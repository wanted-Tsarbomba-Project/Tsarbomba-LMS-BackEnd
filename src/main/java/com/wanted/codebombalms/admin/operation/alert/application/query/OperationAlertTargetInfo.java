package com.wanted.codebombalms.admin.operation.alert.application.query;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;

// 운영 알림 대상인 강좌, 문제 또는 사용자의 표시 정보를 담는다.
public record OperationAlertTargetInfo(
        OperationTargetType targetType,
        Long targetId,
        String title,
        String status,
        String nickname,
        String email,
        Long courseId,
        String courseTitle,
        Long problemSetId,
        String problemSetTitle
) {
}
