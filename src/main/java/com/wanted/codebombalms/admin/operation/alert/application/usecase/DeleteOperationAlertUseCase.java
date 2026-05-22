package com.wanted.codebombalms.admin.operation.alert.application.usecase;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;

//soft delete 전용 유스케이스 인터페이스를 새로 추가
public interface DeleteOperationAlertUseCase {

    OperationAlert delete(Long operationAlertId);
}
