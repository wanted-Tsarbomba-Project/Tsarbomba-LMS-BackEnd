package com.wanted.codebombalms.admin.operation.alert.application.service;

import com.wanted.codebombalms.admin.operation.alert.application.command.UpdateOperationAlertStatusCommand;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.DeleteOperationAlertUseCase;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.UpdateOperationAlertStatusUseCase;
import com.wanted.codebombalms.admin.operation.alert.domain.exception.OperationAlertErrorCode;
import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;
import com.wanted.codebombalms.admin.operation.alert.domain.repository.OperationAlertRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class OperationAlertCommandService implements UpdateOperationAlertStatusUseCase, DeleteOperationAlertUseCase {

    private final OperationAlertRepository operationAlertRepository;

    @Override
    public OperationAlert updateStatus(UpdateOperationAlertStatusCommand command) {
        validateCommand(command);

        OperationAlert operationAlert = operationAlertRepository.findById(command.operationAlertId())
                .orElseThrow(() -> new NotFoundException(OperationAlertErrorCode.OPERATION_ALERT_NOT_FOUND));

        operationAlert.process(
                command.status(),
                command.resolvedBy(),
                LocalDateTime.now()
        );

        return operationAlertRepository.save(operationAlert);
    }

    //DeleteOperationAlertUseCase를 구현
    // operationAlertId 검증 후 삭제되지 않은 알림을 조회하고
    // 도메인의 delete(LocalDateTime.now())를 호출한 뒤 저장
    @Override
    public OperationAlert delete(Long operationAlertId) {
        validateDeleteRequest(operationAlertId);

        OperationAlert operationAlert = operationAlertRepository.findById(operationAlertId)
                .orElseThrow(() -> new NotFoundException(OperationAlertErrorCode.OPERATION_ALERT_NOT_FOUND));

        operationAlert.delete(LocalDateTime.now());

        return operationAlertRepository.save(operationAlert);
    }

    private void validateCommand(UpdateOperationAlertStatusCommand command) {
        if (command == null
                || command.operationAlertId() == null
                || command.status() == null
                || command.resolvedBy() == null) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_STATUS_UPDATE_REQUEST);
        }

    }

    private void validateDeleteRequest(Long operationAlertId) {
        if (operationAlertId == null) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_DELETE_REQUEST);
        }
    }
}
