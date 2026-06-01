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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    //UpdateAutomationRuleUseCase의 구현체, 수정 요청된 규칙만 저장한다.
    @Override
    public List<AutomationRule> update(UpdateAutomationRuleCommand command) {
        validateUpdateCommand(command);

        return command.rules().stream()
                .map(rule -> {
                    AutomationRule automationRule = automationRuleRepository.findById(rule.operationRuleId())
                            .orElseThrow(() -> new NotFoundException(AutomationRuleErrorCode.AUTOMATION_RULE_NOT_FOUND));

                    AutomationRule updatedRule = automationRule.updateThreshold(
                            rule.thresholdValue(),
                            rule.minSampleCount()
                    );

                    return automationRuleRepository.save(updatedRule);
                })
                .toList();
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
        if (command == null || command.rules() == null || command.rules().isEmpty()) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_UPDATE_REQUEST);
        }

        Set<Long> ruleIds = new HashSet<>();
        for (UpdateAutomationRuleCommand.Item rule : command.rules()) {
            if (rule == null
                    || rule.operationRuleId() == null
                    || !ruleIds.add(rule.operationRuleId())) {
                throw new ValidationException(AutomationRuleErrorCode.INVALID_UPDATE_REQUEST);
            }
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
