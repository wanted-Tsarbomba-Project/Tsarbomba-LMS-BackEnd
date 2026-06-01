package com.wanted.codebombalms.admin.operation.alert.application.usecase;

import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertDetail;

// 운영 알림 상세 정보를 조회하는 유스케이스 진입점이다.
public interface GetOperationAlertDetailUseCase {

    // 알림 ID로 알림 기본 정보와 대상 상세 정보를 함께 조회한다.
    OperationAlertDetail getAlertDetail(Long operationAlertId);
}
