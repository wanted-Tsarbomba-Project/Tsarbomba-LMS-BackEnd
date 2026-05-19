package com.wanted.codebombalms.domain.admin.automationrule.repository;

import com.wanted.codebombalms.domain.admin.automationrule.entity.AutomationRule;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationRuleCode;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {

    List<AutomationRule> findByDeletedAtIsNullOrderByCreatedAtDesc();

    List<AutomationRule> findByTargetTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
            OperationTargetType targetType
    );

    boolean existsByRuleCodeAndDeletedAtIsNull(OperationRuleCode ruleCode);

    @Query("""
       select ar.ruleCode
       from AutomationRule ar
       where ar.deletedAt is null
       """)
    List<OperationRuleCode> findRuleCodesByDeletedAtIsNull();

}
