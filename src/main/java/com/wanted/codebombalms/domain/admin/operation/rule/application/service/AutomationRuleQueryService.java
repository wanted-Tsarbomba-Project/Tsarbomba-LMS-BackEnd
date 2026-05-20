package com.wanted.codebombalms.domain.admin.operation.rule.application.service;

import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.domain.admin.operation.rule.application.usecase.GetAutomationRuleOptionsUseCase;
import com.wanted.codebombalms.domain.admin.operation.rule.application.usecase.GetAutomationRulesUseCase;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.model.OperationRuleCode;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.repository.AutomationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutomationRuleQueryService implements GetAutomationRulesUseCase, GetAutomationRuleOptionsUseCase {

    private final AutomationRuleRepository automationRuleRepository;

    @Override
    public List<AutomationRule> getRules(OperationTargetType targetType) {
        return automationRuleRepository.findAllActive(targetType);
    }

    @Override
    public List<OperationRuleCode> getOptions() {
        return Arrays.asList(OperationRuleCode.values());
    }
}