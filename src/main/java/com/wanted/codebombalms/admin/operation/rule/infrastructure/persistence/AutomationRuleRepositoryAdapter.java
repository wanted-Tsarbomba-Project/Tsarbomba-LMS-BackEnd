package com.wanted.codebombalms.admin.operation.rule.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import com.wanted.codebombalms.admin.operation.rule.domain.repository.AutomationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Comparator;
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
                ? springDataRepository.findAllByOrderByOperationRuleIdAsc()
                : springDataRepository.findByRuleCodeIn(findRuleCodesByTargetType(targetType));

        return entities.stream()
                .map(AutomationRuleMapper::toDomain)
                .sorted(Comparator.comparingInt(rule -> getDisplayOrder(rule.getRuleCode())))
                .toList();
    }

    @Override
    public boolean existsActiveByRuleCode(OperationRuleCode ruleCode) {
        return springDataRepository.existsByRuleCode(ruleCode);
    }

    @Override
    public List<OperationRuleCode> findActiveRuleCodes() {
        return springDataRepository.findAllByOrderByOperationRuleIdAsc()
                .stream()
                .map(AutomationRuleJpaEntity::getRuleCode)
                .toList();
    }

    private List<OperationRuleCode> findRuleCodesByTargetType(OperationTargetType targetType) {
        return Arrays.stream(OperationRuleCode.values())
                .filter(ruleCode -> ruleCode.getTargetType() == targetType)
                .toList();
    }

    private int getDisplayOrder(OperationRuleCode ruleCode) {
        return switch (ruleCode) {
            case COURSE_LOW_ENROLLMENT -> 1;
            case PROBLEM_HIGH_WRONG_RATE -> 2;
            case USER_INACTIVE_NO_COURSE -> 3;
        };
    }
}
