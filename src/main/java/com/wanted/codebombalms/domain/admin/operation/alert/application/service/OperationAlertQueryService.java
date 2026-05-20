package com.wanted.codebombalms.domain.admin.operation.alert.application.service;

import com.wanted.codebombalms.domain.admin.operation.alert.application.query.GetOperationAlertsQuery;
import com.wanted.codebombalms.domain.admin.operation.alert.application.usecase.GetOperationAlertsUseCase;
import com.wanted.codebombalms.domain.admin.operation.alert.domain.exception.OperationAlertErrorCode;
import com.wanted.codebombalms.domain.admin.operation.alert.domain.repository.OperationAlertRepository;
import com.wanted.codebombalms.domain.admin.operation.alert.infrastructure.persistence.OperationAlertWithRuleProjection;
import com.wanted.codebombalms.domain.admin.operation.common.application.PageResult;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperationAlertQueryService implements GetOperationAlertsUseCase {

    private final OperationAlertRepository operationAlertRepository;

    @Override
    public PageResult<OperationAlertWithRuleProjection> getAlerts(GetOperationAlertsQuery query) {
        validatePage(query.page(), query.size());

        return operationAlertRepository.findAlerts(
                query.targetType(),
                query.status(),
                query.page(),
                query.size()
        );
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new ValidationException(OperationAlertErrorCode.INVALID_PAGE_REQUEST);
        }
    }
}