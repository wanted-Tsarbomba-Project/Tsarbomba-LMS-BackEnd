package com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;
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

    @Override
    public OperationAlert save(OperationAlert operationAlert) {
        OperationAlertJpaEntity saved = springDataRepository.save(
                OperationAlertMapper.toEntity(operationAlert)
        );

        return OperationAlertMapper.toDomain(saved);
    }
}
