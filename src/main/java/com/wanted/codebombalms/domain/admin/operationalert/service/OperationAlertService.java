package com.wanted.codebombalms.domain.admin.operationalert.service;

import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationTargetType;
import com.wanted.codebombalms.domain.admin.operationalert.dto.request.OperationAlertStatusUpdateRequest;
import com.wanted.codebombalms.domain.admin.operationalert.dto.response.OperationAlertListResponse;
import com.wanted.codebombalms.domain.admin.operationalert.dto.response.OperationAlertStatusUpdateResponse;
import com.wanted.codebombalms.domain.admin.operationalert.entity.OperationAlert;
import com.wanted.codebombalms.domain.admin.operationalert.enums.OperationAlertStatus;
import com.wanted.codebombalms.domain.admin.operationalert.exception.OperationAlertErrorCode;
import com.wanted.codebombalms.domain.admin.operationalert.repository.OperationAlertRepository;
import com.wanted.codebombalms.global.error.exception.NotFoundException;
import com.wanted.codebombalms.global.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperationAlertService {

    private final OperationAlertRepository operationAlertRepository;

    public OperationAlertListResponse findOperationAlerts(
            OperationTargetType targetType,
            OperationAlertStatus status,
            int page,
            int size
    ) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_PAGE_REQUEST);
        }

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "lastDetectedAt")
                        .and(Sort.by(Sort.Direction.DESC, "operationAlertId"))
        );

        Page<OperationAlert> operationAlerts =
                operationAlertRepository.findOperationAlerts(targetType, status, pageRequest);

        return OperationAlertListResponse.from(operationAlerts);
    }

    @Transactional
    public OperationAlertStatusUpdateResponse updateOperationAlertStatus(
            Long operationAlertId,
            OperationAlertStatusUpdateRequest request
    ) {
        validateStatusUpdateRequest(request);

        OperationAlert operationAlert = operationAlertRepository.findById(operationAlertId)
                .orElseThrow(() -> new NotFoundException(OperationAlertErrorCode.OPERATION_ALERT_NOT_FOUND));

        if (operationAlert.getStatus() != OperationAlertStatus.OPEN) {
            throw new ValidationException(OperationAlertErrorCode.ALREADY_PROCESSED_ALERT);
        }

        operationAlert.process(
                request.getStatus(),
                request.getResolvedBy(),
                request.getAdminMemo()
        );

        return OperationAlertStatusUpdateResponse.from(operationAlert);
    }

    private void validateStatusUpdateRequest(OperationAlertStatusUpdateRequest request) {
        if (request == null
                || request.getStatus() == null
                || request.getResolvedBy() == null) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_STATUS_UPDATE_REQUEST);
        }

        if (request.getStatus() != OperationAlertStatus.RESOLVED
                && request.getStatus() != OperationAlertStatus.IGNORED) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_STATUS_UPDATE_REQUEST);
        }

        if (request.getAdminMemo() != null && request.getAdminMemo().length() > 500) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_STATUS_UPDATE_REQUEST);
        }
    }
}