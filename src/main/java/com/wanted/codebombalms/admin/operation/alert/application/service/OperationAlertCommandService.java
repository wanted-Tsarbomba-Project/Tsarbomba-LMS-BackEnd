package com.wanted.codebombalms.admin.operation.alert.application.service;

import com.wanted.codebombalms.admin.operation.alert.application.command.UpdateOperationAlertStatusCommand;
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
public class OperationAlertCommandService implements UpdateOperationAlertStatusUseCase {

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

    private void validateCommand(UpdateOperationAlertStatusCommand command) {
        if (command == null
                || command.operationAlertId() == null
                || command.status() == null
                || command.resolvedBy() == null) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_STATUS_UPDATE_REQUEST);
        }

    }
}
