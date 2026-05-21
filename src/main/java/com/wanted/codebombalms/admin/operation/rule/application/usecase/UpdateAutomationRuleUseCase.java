package com.wanted.codebombalms.admin.operation.rule.application.usecase;

import com.wanted.codebombalms.admin.operation.rule.application.command.UpdateAutomationRuleCommand;
import com.wanted.codebombalms.admin.operation.rule.domain.model.AutomationRule;

//threshold 수정 기능의 application boundary/ 컨트롤러는 이 인터페이스에 의존
public interface UpdateAutomationRuleUseCase {

    AutomationRule update(UpdateAutomationRuleCommand command);
}
