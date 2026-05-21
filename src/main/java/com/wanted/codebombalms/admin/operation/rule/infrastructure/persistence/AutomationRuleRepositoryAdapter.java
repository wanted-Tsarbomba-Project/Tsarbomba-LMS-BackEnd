package com.wanted.codebombalms.admin.operation.rule.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import com.wanted.codebombalms.admin.operation.rule.domain.repository.AutomationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AutomationRuleRepositoryAdapter implements AutomationRuleRepository {

    private final SpringDataAutomationRuleRepository springDataRepository;

    @Override
    public AutomationRule save(AutomationRule automationRule) {
        AutomationRuleJpaEntity saved = springDataRepository.save(
                AutomationRuleMapper.toEntity(automationRule)
        );
        return AutomationRuleMapper.toDomain(saved);
    }

    @Override
    public Optional<AutomationRule> findById(Long operationRuleId) {
        return springDataRepository.findById(operationRuleId)
                .map(AutomationRuleMapper::toDomain);
    }

    @Override
    public List<AutomationRule> findAllActive(OperationTargetType targetType) {
        List<AutomationRuleJpaEntity> entities = targetType == null
                ? springDataRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
                : springDataRepository.findByTargetTypeAndDeletedAtIsNullOrderByCreatedAtDesc(targetType);

        return entities.stream()
                .map(AutomationRuleMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByRuleCode(OperationRuleCode ruleCode) {
        return springDataRepository.existsByRuleCodeAndDeletedAtIsNull(ruleCode);
    }

    @Override
    public List<OperationRuleCode> findActiveRuleCodes() {
        return springDataRepository.findRuleCodesByDeletedAtIsNull();
    }
}