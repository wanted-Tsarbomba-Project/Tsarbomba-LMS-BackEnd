package com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.alert.application.query.GetOperationAlertsQuery;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertListItem;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertQueryRepository;
import com.wanted.codebombalms.admin.operation.common.application.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OperationAlertQueryAdapter implements OperationAlertQueryRepository {

    private final SpringDataOperationAlertRepository springDataRepository;

    @Override
    public PageResult<OperationAlertListItem> findAlerts(GetOperationAlertsQuery query) {
        PageRequest pageRequest = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(Sort.Direction.DESC, "lastDetectedAt")
                        .and(Sort.by(Sort.Direction.DESC, "operationAlertId"))
        );

        Page<OperationAlertWithRuleProjection> result = springDataRepository.findAlerts(
                query.targetType(),
                query.status(),
                pageRequest
        );

        List<OperationAlertListItem> content = result.getContent().stream()
                .map(this::toListItem)
                .toList();

        return new PageResult<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    private OperationAlertListItem toListItem(OperationAlertWithRuleProjection projection) {
        return new OperationAlertListItem(
                projection.operationAlertId(),
                projection.operationRuleId(),
                projection.targetType(),
                projection.targetId(),
                projection.detectedValue(),
                projection.thresholdValueSnapshot(),
                projection.severity(),
                projection.status(),
                projection.assigneeId(),
                projection.reason(),
                projection.recommendedAction(),
                projection.firstDetectedAt(),
                projection.lastDetectedAt()
        );
    }
}
