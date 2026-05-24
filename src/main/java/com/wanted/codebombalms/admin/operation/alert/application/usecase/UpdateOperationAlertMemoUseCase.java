package com.wanted.codebombalms.admin.operation.alert.application.usecase;

import com.wanted.codebombalms.admin.operation.alert.application.command.UpdateOperationAlertMemoCommand;
import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;

// 운영 알림 관리자 메모를 수정하는 유스케이스 진입점이다.
public interface UpdateOperationAlertMemoUseCase {

    // 알림 ID와 메모 내용으로 관리자 메모를 수정한다.
    OperationAlert updateMemo(UpdateOperationAlertMemoCommand command);
}
