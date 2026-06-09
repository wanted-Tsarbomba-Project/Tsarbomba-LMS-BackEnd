package com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.alert.application.query.GetOperationAlertsQuery;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertDetail;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertListItem;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertQueryRepository;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertRuleInfo;
import com.wanted.codebombalms.admin.operation.common.application.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Override
    // 알림과 규칙을 조인해 상세 조회 기본 정보를 가져온다.
    public Optional<OperationAlertDetail> findAlertDetail(Long operationAlertId) {
        return springDataRepository.findAlertDetail(operationAlertId)
                .map(this::toDetail);
    }

    private OperationAlertListItem toListItem(OperationAlertWithRuleProjection projection) {
        return new OperationAlertListItem(
                projection.operationAlertId(),
                projection.status(),
                projection.recommendedAction()
        );
    }

    // 상세 조회 Projection을 application 조회 결과로 변환한다.
    private OperationAlertDetail toDetail(OperationAlertDetailProjection projection) {
        return new OperationAlertDetail(
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
                projection.lastDetectedAt(),
                projection.resolvedBy(),
                projection.resolvedAt(),
                projection.adminMemo(),
                projection.createdAt(),
                projection.updatedAt(),
                OperationAlertRuleInfo.from(projection.ruleCode(), projection.minSampleCount()),
                null,
                null,
                null
        );
    }
}
