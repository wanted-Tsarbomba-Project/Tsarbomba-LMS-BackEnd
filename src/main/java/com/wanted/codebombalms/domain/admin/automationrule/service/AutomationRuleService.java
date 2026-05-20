package com.wanted.codebombalms.domain.admin.automationrule.service;

import com.wanted.codebombalms.domain.admin.automationrule.dto.request.AutomationRuleCreateRequest;
import com.wanted.codebombalms.domain.admin.automationrule.dto.response.AutomationRuleOptionsResponse;
import com.wanted.codebombalms.domain.admin.automationrule.dto.response.AutomationRuleResponse;
import com.wanted.codebombalms.domain.admin.automationrule.entity.AutomationRule;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationRuleCode;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationSeverity;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationTargetType;
import com.wanted.codebombalms.domain.admin.automationrule.exception.AutomationRuleErrorCode;
import com.wanted.codebombalms.domain.admin.automationrule.repository.AutomationRuleRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutomationRuleService {

    private final AutomationRuleRepository automationRuleRepository;

    public List<AutomationRuleResponse> findAutomationRules(OperationTargetType targetType) {
        List<AutomationRule> rules = targetType == null
                ? automationRuleRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
                : automationRuleRepository.findByTargetTypeAndDeletedAtIsNullOrderByCreatedAtDesc(targetType);

        return rules.stream()
                .map(AutomationRuleResponse::from)
                .toList();
    }

    public AutomationRuleOptionsResponse findAutomationRuleOptions(OperationTargetType targetType) {
        List<OperationRuleCode> registeredRuleCodes =
                automationRuleRepository.findRuleCodesByDeletedAtIsNull();

        List<OperationRuleCode> ruleCodes = Arrays.stream(OperationRuleCode.values())
                .filter(ruleCode -> targetType == null || ruleCode.getTargetType() == targetType)
                .filter(ruleCode -> !registeredRuleCodes.contains(ruleCode))
                .toList();

        return AutomationRuleOptionsResponse.from(
                ruleCodes,
                Arrays.asList(OperationSeverity.values())
        );
    }

    @Transactional
    public AutomationRuleResponse createAutomationRule(
            AutomationRuleCreateRequest request,
            Long createdBy
    ) {
        validateCreateRequest(request, createdBy);

        OperationRuleCode ruleCode = request.getRuleCode();

        if (automationRuleRepository.existsByRuleCodeAndDeletedAtIsNull(ruleCode)) {
            throw new ConflictException(AutomationRuleErrorCode.DUPLICATED_RULE_CODE);
        }

        AutomationRule rule = AutomationRule.create(
                ruleCode,
                ruleCode.getLabel(),
                ruleCode.getTargetType(),
                request.getThresholdValue(),
                normalizeMinSampleCount(ruleCode, request.getMinSampleCount()),
                request.getSeverity() == null ? OperationSeverity.MEDIUM : request.getSeverity(),
                request.getEnabled() == null || request.getEnabled(),
                createdBy
        );

        return AutomationRuleResponse.from(automationRuleRepository.save(rule));
    }

    private void validateCreateRequest(
            AutomationRuleCreateRequest request,
            Long createdBy
    ) {
        if (request == null
                || request.getRuleCode() == null
                || request.getThresholdValue() == null
                || createdBy == null) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_CREATE_REQUEST);
        }


        OperationRuleCode ruleCode = request.getRuleCode();

        if (request.getThresholdValue().compareTo(ruleCode.getThresholdMin()) < 0) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_THRESHOLD_VALUE);
        }

        if (ruleCode.getThresholdMax() != null
                && request.getThresholdValue().compareTo(ruleCode.getThresholdMax()) > 0) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_THRESHOLD_VALUE);
        }

        if (ruleCode.isRequiresMinSampleCount()
                && (request.getMinSampleCount() == null || request.getMinSampleCount() <= 0)) {
            throw new ValidationException(AutomationRuleErrorCode.INVALID_MIN_SAMPLE_COUNT);
        }
    }

    private Integer normalizeMinSampleCount(OperationRuleCode ruleCode, Integer minSampleCount) {
        if (!ruleCode.isRequiresMinSampleCount()) {
            return null;
        }

        return minSampleCount;
    }
}