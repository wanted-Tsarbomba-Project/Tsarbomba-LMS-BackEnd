package com.wanted.codebombalms.admin.operation.alert.application.service;

import com.wanted.codebombalms.admin.operation.alert.application.command.UpdateOperationAlertMemoCommand;
import com.wanted.codebombalms.admin.operation.alert.application.command.UpdateOperationAlertStatusCommand;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.DeleteOperationAlertUseCase;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.UpdateOperationAlertMemoUseCase;
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
public class OperationAlertCommandService implements
        UpdateOperationAlertStatusUseCase,
        UpdateOperationAlertMemoUseCase,
        DeleteOperationAlertUseCase {

    private static final int MAX_ADMIN_MEMO_LENGTH = 500;

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

    @Override
    // 운영 알림을 조회한 뒤 관리자 메모를 수정하고 저장한다.
    public OperationAlert updateMemo(UpdateOperationAlertMemoCommand command) {
        validateMemoCommand(command);

        OperationAlert operationAlert = operationAlertRepository.findById(command.operationAlertId())
                .orElseThrow(() -> new NotFoundException(OperationAlertErrorCode.OPERATION_ALERT_NOT_FOUND));

        operationAlert.updateAdminMemo(
                normalizeMemo(command.adminMemo()),
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

    // 메모 수정 요청의 알림 ID와 메모 길이를 검증한다.
    private void validateMemoCommand(UpdateOperationAlertMemoCommand command) {
        if (command == null || command.operationAlertId() == null) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_MEMO_UPDATE_REQUEST);
        }

        String adminMemo = command.adminMemo();
        if (adminMemo != null && adminMemo.length() > MAX_ADMIN_MEMO_LENGTH) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_MEMO_UPDATE_REQUEST);
        }
    }

    // null 또는 공백 메모는 null로, 값이 있는 메모는 앞뒤 공백을 제거해 정규화한다.
    private String normalizeMemo(String adminMemo) {
        if (adminMemo == null || adminMemo.isBlank()) {
            return null;
        }

        return adminMemo.trim();
    }
}
