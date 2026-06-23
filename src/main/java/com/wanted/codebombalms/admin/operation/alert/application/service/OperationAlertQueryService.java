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
import com.wanted.codebombalms.admin.operation.metrics.AdminMetrics;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperationAlertQueryService implements GetOperationAlertsUseCase, GetOperationAlertDetailUseCase {

    private final OperationAlertQueryRepository operationAlertQueryRepository;
    private final OperationAlertTargetDetailPort operationAlertTargetDetailPort;
    private final AdminMetrics adminMetrics;

    @Override
    public PageResult<OperationAlertListItem> getAlerts(GetOperationAlertsQuery query) {
        validatePage(query.page(), query.size());

        // 운영 알림 목록 조회, 조회 전후 시간을 재서 admin_operation_alert_list_query_duration에 기록하고, Loki 용 로그를 남김
        long startedAt = System.nanoTime();
        PageResult<OperationAlertListItem> result = operationAlertQueryRepository.findAlerts(query);
        long elapsedNanos = System.nanoTime() - startedAt;

        adminMetrics.recordAlertListQuery(elapsedNanos);
        log.info("event=admin_operation_alert_list_queried targetType={} status={} resultCount={} totalElements={} durationMs={}",
                query.targetType(), query.status(), result.getContent().size(), result.getTotalElements(),
                elapsedNanos / 1_000_000);

        return result;
    }

    @Override
    // 알림 상세 기본 정보와 대상 도메인 상세 정보를 조합해 반환한다.
    public OperationAlertDetail getAlertDetail(Long operationAlertId) {
        long startedAt = System.nanoTime();
        OperationAlertDetail alertDetail = operationAlertQueryRepository.findAlertDetail(operationAlertId)
                .orElseThrow(() -> new NotFoundException(OperationAlertErrorCode.OPERATION_ALERT_NOT_FOUND));
        long elapsedNanos = System.nanoTime() - startedAt;

        adminMetrics.recordAlertDetailQuery(elapsedNanos);
        log.info("event=admin_operation_alert_detail_queried targetType={} durationMs={}",
                alertDetail.targetType(), elapsedNanos / 1_000_000);

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
