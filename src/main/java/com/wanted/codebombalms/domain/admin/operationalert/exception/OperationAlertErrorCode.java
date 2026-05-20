package com.wanted.codebombalms.domain.admin.operationalert.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperationAlertErrorCode implements ErrorCode {

    INVALID_SEARCH_CONDITION(400, "OA-001", "알림 조회 조건이 올바르지 않습니다."),
    INVALID_PAGE_REQUEST(400, "OA-002", "페이지 요청 값이 올바르지 않습니다."),
    OPERATION_ALERT_NOT_FOUND(404, "OA-003", "운영 알림을 찾을 수 없습니다."),
    INVALID_STATUS_UPDATE_REQUEST(400, "OA-004", "운영 알림 상태 변경 요청이 올바르지 않습니다."),
    ALREADY_PROCESSED_ALERT(400, "OA-005", "이미 처리된 운영 알림입니다.");

    private final int status;
    private final String code;
    private final String message;
}