package com.wanted.codebombalms.domain.admin.operation.alert.infrastructure.persistence;

import com.wanted.codebombalms.domain.admin.operation.alert.domain.model.OperationAlert;
import com.wanted.codebombalms.domain.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.domain.admin.operation.alert.domain.repository.OperationAlertRepository;
import com.wanted.codebombalms.domain.admin.operation.common.application.PageResult;
import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OperationAlertRepositoryAdapter implements OperationAlertRepository {

    private final SpringDataOperationAlertRepository springDataRepository;

    @Override
    public Optional<OperationAlert> findById(Long operationAlertId) {
        return springDataRepository.findById(operationAlertId)
                .map(OperationAlertMapper::toDomain);
    }

    @Override
    public OperationAlert save(OperationAlert operationAlert) {
        OperationAlertJpaEntity saved = springDataRepository.save(
                OperationAlertMapper.toEntity(operationAlert)
        );

        return OperationAlertMapper.toDomain(saved);
    }

    @Override
    public PageResult<OperationAlertWithRuleProjection> findAlerts(
            OperationTargetType targetType,
            OperationAlertStatus status,
            int page,
            int size
    ) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "lastDetectedAt")
                        .and(Sort.by(Sort.Direction.DESC, "operationAlertId"))
        );

        Page<OperationAlertWithRuleProjection> result = springDataRepository.findAlerts(
                targetType,
                status,
                pageRequest
        );

        return new PageResult<>(
                result.getContent(),
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
}