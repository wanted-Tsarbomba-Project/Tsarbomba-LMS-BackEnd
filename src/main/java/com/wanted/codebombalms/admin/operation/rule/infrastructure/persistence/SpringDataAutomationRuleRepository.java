package com.wanted.codebombalms.admin.operation.rule.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAutomationRuleRepository extends JpaRepository<AutomationRuleJpaEntity, Long> {

    List<AutomationRuleJpaEntity> findAllByOrderByOperationRuleIdAsc();

    // 활성화된 자동 규칙을 등록 순서대로 조회한다.
    List<AutomationRuleJpaEntity> findByEnabledTrueOrderByOperationRuleIdAsc();

    List<AutomationRuleJpaEntity> findByRuleCodeIn(List<OperationRuleCode> ruleCodes);
}
