package com.wanted.codebombalms.domain.admin.operation.rule.application.usecase;

import com.wanted.codebombalms.domain.admin.operation.rule.domain.model.OperationRuleCode;

import java.util.List;

public interface GetAutomationRuleOptionsUseCase {
    List<OperationRuleCode> getOptions();
}