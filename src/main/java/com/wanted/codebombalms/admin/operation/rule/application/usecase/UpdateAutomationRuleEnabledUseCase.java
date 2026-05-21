package com.wanted.codebombalms.admin.operation.rule.application.usecase;

import com.wanted.codebombalms.admin.operation.rule.application.command.UpdateAutomationRuleEnabledCommand;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;

// 컨트롤러가 의존하는 enabled 변경 usecase 인터페이스
public interface UpdateAutomationRuleEnabledUseCase {

    AutomationRule updateEnabled(UpdateAutomationRuleEnabledCommand command);
}
