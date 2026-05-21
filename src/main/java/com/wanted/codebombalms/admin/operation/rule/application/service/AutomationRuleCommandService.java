package com.wanted.codebombalms.admin.operation.rule.application.service;

import com.wanted.codebombalms.admin.operation.rule.application.command.CreateAutomationRuleCommand;
import com.wanted.codebombalms.admin.operation.rule.application.command.UpdateAutomationRuleEnabledCommand;
import com.wanted.codebombalms.admin.operation.rule.application.command.UpdateAutomationRuleCommand;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.CreateAutomationRuleUseCase;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.UpdateAutomationRuleEnabledUseCase;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.UpdateAutomationRuleUseCase;
import com.wanted.codebombalms.admin.operation.rule.domain.exception.AutomationRuleErrorCode;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;
import com.wanted.codebombalms.admin.operation.rule.domain.repository.AutomationRuleRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AutomationRuleCommandService implements CreateAutomationRuleUseCase, UpdateAutomationRuleUseCase, UpdateAutomationRuleEnabledUseCase {

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

    //UpdateAutomationRuleUseCase의 구현체, id로 기존 규칙을 찾고, 없으면 404를 던진다.
    @Override
    public AutomationRule update(UpdateAutomationRuleCommand command) {
        validateUpdateCommand(command);

        AutomationRule automationRule = automationRuleRepository.findById(command.operationRuleId())
                .orElseThrow(() -> new NotFoundException(AutomationRuleErrorCode.AUTOMATION_RULE_NOT_FOUND));

        AutomationRule updatedRule = automationRule.updateThreshold(
                command.thresholdValue(),
                command.minSampleCount()
        );

        return automationRuleRepository.save(updatedRule);
    }

    //enabled 변경 usecase를 구현
    @Override
    public AutomationRule updateEnabled(UpdateAutomationRuleEnabledCommand command) {
        validateUpdateEnabledCommand(command);

        AutomationRule automationRule = automationRuleRepository.findById(command.operationRuleId())
                .orElseThrow(() -> new NotFoundException(AutomationRuleErrorCode.AUTOMATION_RULE_NOT_FOUND));

        AutomationRule updatedRule = automationRule.updateEnabled(command.enabled());

        return automationRuleRepository.save(updatedRule);
    }

    private void validateCreateCommand(CreateAutomationRuleCommand command) {
        if (command == null) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_CREATE_REQUEST);
        }
    }

    private void validateUpdateCommand(UpdateAutomationRuleCommand command) {
        if (command == null || command.operationRuleId() == null) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_UPDATE_REQUEST);
        }
    }

    private void validateUpdateEnabledCommand(UpdateAutomationRuleEnabledCommand command) {
        if (command == null
                || command.operationRuleId() == null
                || command.enabled() == null) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_ENABLED_UPDATE_REQUEST);
        }
    }
}
