package com.wanted.codebombalms.admin.operation.alert.application.service;

import com.wanted.codebombalms.admin.operation.alert.application.query.GetOperationAlertsQuery;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertDetail;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertListItem;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertQueryRepository;
import com.wanted.codebombalms.admin.operation.alert.application.port.OperationAlertTargetDetailPort;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.GetOperationAlertDetailUseCase;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.GetOperationAlertsUseCase;
import com.wanted.codebombalms.admin.operation.alert.domain.exception.OperationAlertErrorCode;
import com.wanted.codebombalms.admin.operation.common.application.PageResult;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperationAlertQueryService implements GetOperationAlertsUseCase, GetOperationAlertDetailUseCase {

    private final OperationAlertQueryRepository operationAlertQueryRepository;
    private final OperationAlertTargetDetailPort operationAlertTargetDetailPort;

    @Override
    public PageResult<OperationAlertListItem> getAlerts(GetOperationAlertsQuery query) {
        validatePage(query.page(), query.size());
        return operationAlertQueryRepository.findAlerts(query);
    }

    @Override
    // 알림 상세 기본 정보와 대상 도메인 상세 정보를 조합해 반환한다.
    public OperationAlertDetail getAlertDetail(Long operationAlertId) {
        OperationAlertDetail alertDetail = operationAlertQueryRepository.findAlertDetail(operationAlertId)
                .orElseThrow(() -> new NotFoundException(OperationAlertErrorCode.OPERATION_ALERT_NOT_FOUND));

        return alertDetail.withTargetDetail(operationAlertTargetDetailPort.loadTargetDetail(
                alertDetail.targetType(),
                alertDetail.targetId(),
                alertDetail.detectedValue(),
                alertDetail.thresholdValueSnapshot(),
                alertDetail.rule()
        ));
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_PAGE_REQUEST);
        }
    }
}
