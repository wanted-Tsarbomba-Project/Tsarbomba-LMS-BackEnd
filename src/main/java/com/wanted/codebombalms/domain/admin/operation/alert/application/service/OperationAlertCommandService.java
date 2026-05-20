package com.wanted.codebombalms.domain.admin.operation.alert.application.service;

import com.wanted.codebombalms.domain.admin.operation.alert.application.command.UpdateOperationAlertStatusCommand;
import com.wanted.codebombalms.domain.admin.operation.alert.application.usecase.UpdateOperationAlertStatusUseCase;
import com.wanted.codebombalms.domain.admin.operation.alert.domain.exception.OperationAlertErrorCode;
import com.wanted.codebombalms.domain.admin.operation.alert.domain.model.OperationAlert;
import com.wanted.codebombalms.domain.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.domain.admin.operation.alert.domain.repository.OperationAlertRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OperationAlertCommandService implements UpdateOperationAlertStatusUseCase {

    private final OperationAlertRepository operationAlertRepository;

    @Override
    public OperationAlert updateStatus(UpdateOperationAlertStatusCommand command) {
        validateCommand(command);

        OperationAlert operationAlert = operationAlertRepository.findById(command.operationAlertId())
                .orElseThrow(() -> new NotFoundException(OperationAlertErrorCode.OPERATION_ALERT_NOT_FOUND));

        if (operationAlert.getStatus() != OperationAlertStatus.OPEN) {
            throw new ValidationException(OperationAlertErrorCode.ALREADY_PROCESSED_ALERT);
        }

        try {
            operationAlert.process(
                    command.status(),
                    command.resolvedBy(),
                    command.adminMemo()
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_STATUS_UPDATE_REQUEST);
        }

        return operationAlertRepository.save(operationAlert);
    }

    private void validateCommand(UpdateOperationAlertStatusCommand command) {
        if (command == null
                || command.operationAlertId() == null
                || command.status() == null
                || command.resolvedBy() == null) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_STATUS_UPDATE_REQUEST);
        }

        if (command.status() != OperationAlertStatus.RESOLVED
                && command.status() != OperationAlertStatus.IGNORED) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_STATUS_UPDATE_REQUEST);
        }

        if (command.adminMemo() != null && command.adminMemo().length() > 500) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_STATUS_UPDATE_REQUEST);
        }
    }
}