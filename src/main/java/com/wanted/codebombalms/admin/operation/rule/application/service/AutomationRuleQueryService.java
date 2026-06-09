package com.wanted.codebombalms.admin.operation.rule.application.service;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.GetAutomationRulesUseCase;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.repository.AutomationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutomationRuleQueryService implements GetAutomationRulesUseCase {

    private final AutomationRuleRepository automationRuleRepository;

    @Override
    public List<AutomationRule> getRules(OperationTargetType targetType) {
        return automationRuleRepository.findAllActive(targetType);
    }
}
