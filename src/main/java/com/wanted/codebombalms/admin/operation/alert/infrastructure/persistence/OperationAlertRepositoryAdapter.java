package com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;
import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.alert.domain.repository.OperationAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OperationAlertRepositoryAdapter implements OperationAlertRepository {

    private final SpringDataOperationAlertRepository springDataRepository;

    @Override
    public Optional<OperationAlert> findById(Long operationAlertId) {
        return springDataRepository.findByOperationAlertIdAndDeletedAtIsNull(operationAlertId)
                .map(OperationAlertMapper::toDomain);
    }

    // 같은 규칙과 대상에 대해 아직 처리되지 않은 알림을 조회한다.
    @Override
    public Optional<OperationAlert> findOpenByRuleIdAndTarget(
            Long operationRuleId,
            OperationTargetType targetType,
            Long targetId
    ) {
        return springDataRepository
                .findByOperationRuleIdAndTargetTypeAndTargetIdAndStatusAndDeletedAtIsNull(
                        operationRuleId,
                        targetType,
                        targetId,
                        OperationAlertStatus.OPEN
                )
                .map(OperationAlertMapper::toDomain);
    }

    @Override
    public OperationAlert save(OperationAlert operationAlert) {
        OperationAlertJpaEntity saved = springDataRepository.save(
                OperationAlertMapper.toEntity(operationAlert)
        );

        return OperationAlertMapper.toDomain(saved);
    }
}
