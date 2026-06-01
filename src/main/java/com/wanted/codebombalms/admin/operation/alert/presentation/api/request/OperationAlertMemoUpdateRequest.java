package com.wanted.codebombalms.admin.operation.alert.presentation.api.request;

import com.wanted.codebombalms.admin.operation.alert.application.command.UpdateOperationAlertMemoCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
// 운영 알림 관리자 메모 수정 요청을 받는다.
public class OperationAlertMemoUpdateRequest {

    private String adminMemo;

    // path variable의 알림 ID와 요청 본문의 메모를 command로 변환한다.
    public UpdateOperationAlertMemoCommand toCommand(Long operationAlertId) {
        return new UpdateOperationAlertMemoCommand(operationAlertId, adminMemo);
    }
}
