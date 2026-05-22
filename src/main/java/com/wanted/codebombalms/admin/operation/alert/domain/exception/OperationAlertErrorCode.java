package com.wanted.codebombalms.admin.operation.alert.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperationAlertErrorCode implements ErrorCode {

    // 조회
    INVALID_SEARCH_CONDITION("ADM-ALT-001", "알림 조회 조건이 올바르지 않습니다."),
    INVALID_PAGE_REQUEST("ADM-ALT-002", "페이지 요청 값이 올바르지 않습니다."),
    OPERATION_ALERT_NOT_FOUND("ADM-ALT-003", "운영 알림을 찾을 수 없습니다."),

    // 상태 변경
    INVALID_STATUS_UPDATE_REQUEST("ADM-ALT-004", "운영 알림 상태 변경 요청이 올바르지 않습니다."),
    ALREADY_PROCESSED_ALERT("ADM-ALT-005", "이미 처리된 운영 알림입니다."),

    // 삭제
    INVALID_DELETE_REQUEST("ADM-ALT-006", "운영 알림 삭제 요청이 올바르지 않습니다."),
    CANNOT_DELETE_OPEN_ALERT("ADM-ALT-007", "OPEN 상태의 운영 알림은 삭제할 수 없습니다.");

    private final String code;
    private final String message;
}
