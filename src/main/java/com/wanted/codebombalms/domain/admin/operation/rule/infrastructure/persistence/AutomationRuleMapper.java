package com.wanted.codebombalms.domain.admin.operation.rule.infrastructure.persistence;

import com.wanted.codebombalms.domain.admin.operation.rule.domain.model.AutomationRule;

public class AutomationRuleMapper {

    private AutomationRuleMapper() {
    }

    public static AutomationRule toDomain(AutomationRuleJpaEntity entity) {
        return AutomationRule.restore(
                entity.getOperationRuleId(),
                entity.getCreatedBy(),
                entity.getRuleCode(),
                entity.getRuleName(),
                entity.getTargetType(),
                entity.getThresholdValue(),
                entity.getMinSampleCount(),
                entity.getSeverity(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public static AutomationRuleJpaEntity toEntity(AutomationRule domain) {
        return new AutomationRuleJpaEntity(
                domain.getOperationRuleId(),
                domain.getCreatedBy(),
                domain.getRuleCode(),
                domain.getRuleName(),
                domain.getTargetType(),
                domain.getThresholdValue(),
                domain.getMinSampleCount(),
                domain.getSeverity(),
                domain.isEnabled(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getDeletedAt()
        );
    }
}