package com.wanted.codebombalms.admin.operation.rule.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;

public class AutomationRuleMapper {

    private AutomationRuleMapper() {
    }

    public static AutomationRule toDomain(AutomationRuleJpaEntity entity) {
        return AutomationRule.restore(
                entity.getOperationRuleId(),
                entity.getCreatedBy(),
                entity.getRuleCode(),
                entity.getThresholdValue(),
                entity.getMinSampleCount(),
                entity.getSeverity(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static AutomationRuleJpaEntity toEntity(AutomationRule domain) {
        return new AutomationRuleJpaEntity(
                domain.getOperationRuleId(),
                domain.getCreatedBy(),
                domain.getRuleCode(),
                domain.getThresholdValue(),
                domain.getMinSampleCount(),
                domain.getSeverity(),
                domain.isEnabled(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
