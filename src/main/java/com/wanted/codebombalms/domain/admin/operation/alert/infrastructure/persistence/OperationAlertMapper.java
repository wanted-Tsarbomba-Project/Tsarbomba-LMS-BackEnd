package com.wanted.codebombalms.domain.admin.operation.alert.infrastructure.persistence;

import com.wanted.codebombalms.domain.admin.operation.alert.domain.model.OperationAlert;

public class OperationAlertMapper {

    private OperationAlertMapper() {
    }

    public static OperationAlert toDomain(OperationAlertJpaEntity entity) {
        return OperationAlert.restore(
                entity.getOperationAlertId(),
                entity.getOperationRuleId(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getDetectedValue(),
                entity.getThresholdValueSnapshot(),
                entity.getAssigneeId(),
                entity.getReason(),
                entity.getRecommendedAction(),
                entity.getFirstDetectedAt(),
                entity.getLastDetectedAt(),
                entity.getStatus(),
                entity.getResolvedBy(),
                entity.getResolvedAt(),
                entity.getAdminMemo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static OperationAlertJpaEntity toEntity(OperationAlert domain) {
        return new OperationAlertJpaEntity(
                domain.getOperationAlertId(),
                domain.getOperationRuleId(),
                domain.getTargetType(),
                domain.getTargetId(),
                domain.getDetectedValue(),
                domain.getThresholdValueSnapshot(),
                domain.getAssigneeId(),
                domain.getReason(),
                domain.getRecommendedAction(),
                domain.getFirstDetectedAt(),
                domain.getLastDetectedAt(),
                domain.getStatus(),
                domain.getResolvedBy(),
                domain.getResolvedAt(),
                domain.getAdminMemo(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}