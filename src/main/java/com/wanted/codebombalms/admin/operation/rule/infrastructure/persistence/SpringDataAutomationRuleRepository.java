package com.wanted.codebombalms.admin.operation.rule.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAutomationRuleRepository extends JpaRepository<AutomationRuleJpaEntity, Long> {

    List<AutomationRuleJpaEntity> findAllByOrderByOperationRuleIdAsc();

    boolean existsByRuleCode(OperationRuleCode ruleCode);

    List<AutomationRuleJpaEntity> findByRuleCodeIn(List<OperationRuleCode> ruleCodes);
}
