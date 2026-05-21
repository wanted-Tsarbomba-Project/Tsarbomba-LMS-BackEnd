package com.wanted.codebombalms.admin.operation.rule.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataAutomationRuleRepository extends JpaRepository<AutomationRuleJpaEntity, Long> {

    List<AutomationRuleJpaEntity> findByDeletedAtIsNullOrderByCreatedAtDesc();

    List<AutomationRuleJpaEntity> findByTargetTypeAndDeletedAtIsNullOrderByCreatedAtDesc(OperationTargetType targetType);

    boolean existsByRuleCodeAndDeletedAtIsNull(OperationRuleCode ruleCode);

    @Query("select ar.ruleCode from AutomationRuleJpaEntity ar where ar.deletedAt is null")
    List<OperationRuleCode> findRuleCodesByDeletedAtIsNull();
}