package com.wanted.codebombalms.domain.admin.operation.rule.application.service;

import com.wanted.codebombalms.domain.admin.operation.rule.application.command.CreateAutomationRuleCommand;
import com.wanted.codebombalms.domain.admin.operation.rule.application.usecase.CreateAutomationRuleUseCase;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.exception.AutomationRuleErrorCode;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.model.OperationRuleCode;
import com.wanted.codebombalms.domain.admin.operation.rule.domain.repository.AutomationRuleRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AutomationRuleCommandService implements CreateAutomationRuleUseCase {

    private final AutomationRuleRepository automationRuleRepository;

    @Override
    public AutomationRule create(CreateAutomationRuleCommand command) {
        validateCreateCommand(command);

        if (automationRuleRepository.existsActiveByRuleCode(command.ruleCode())) {
            throw new ConflictException(AutomationRuleErrorCode.DUPLICATED_RULE_CODE);
        }

        AutomationRule automationRule = AutomationRule.create(
                command.createdBy(),
                command.ruleCode(),
                command.thresholdValue(),
                command.minSampleCount(),
                command.severity(),
                command.enabled()
        );

        return automationRuleRepository.save(automationRule);
    }

    private void validateCreateCommand(CreateAutomationRuleCommand command) {
        if (command == null
                || command.createdBy() == null
                || command.ruleCode() == null
                || command.thresholdValue() == null) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_CREATE_REQUEST);
        }

        OperationRuleCode ruleCode = command.ruleCode();

        if (!ruleCode.isValidThreshold(command.thresholdValue())) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_THRESHOLD_VALUE);
        }

        if (ruleCode.isRequiresMinSampleCount()
                && (command.minSampleCount() == null || command.minSampleCount() <= 0)) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_MIN_SAMPLE_COUNT);
        }
    }
}
