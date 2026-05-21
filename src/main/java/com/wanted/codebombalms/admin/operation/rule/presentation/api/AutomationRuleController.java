package com.wanted.codebombalms.admin.operation.rule.presentation.api;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.CreateAutomationRuleUseCase;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.GetAutomationRuleOptionsUseCase;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.GetAutomationRulesUseCase;
import com.wanted.codebombalms.admin.operation.rule.presentation.api.request.AutomationRuleCreateRequest;
import com.wanted.codebombalms.admin.operation.rule.presentation.api.response.AutomationRuleOptionsResponse;
import com.wanted.codebombalms.admin.operation.rule.presentation.api.response.AutomationRuleResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/automation-rules")
@RequiredArgsConstructor
public class AutomationRuleController {

    private final GetAutomationRulesUseCase getAutomationRulesUseCase;
    private final GetAutomationRuleOptionsUseCase getAutomationRuleOptionsUseCase;
    private final CreateAutomationRuleUseCase createAutomationRuleUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AutomationRuleResponse>>> findAutomationRules(
            @RequestParam(required = false) OperationTargetType targetType
    ) {
        List<AutomationRuleResponse> response = getAutomationRulesUseCase.getRules(targetType)
                .stream()
                .map(AutomationRuleResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                AutomationRuleResponseCode.RETRIEVED,
                AutomationRuleResponseMessage.RETRIEVED,
                response
        ));
    }

    @GetMapping("/options")
    public ResponseEntity<ApiResponse<AutomationRuleOptionsResponse>> findAutomationRuleOptions() {
        AutomationRuleOptionsResponse response = AutomationRuleOptionsResponse.from(
                getAutomationRuleOptionsUseCase.getOptions()
        );

        return ResponseEntity.ok(ApiResponse.success(
                AutomationRuleResponseCode.RETRIEVED,
                AutomationRuleResponseMessage.RETRIEVED,
                response
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> createAutomationRule(
            @AuthenticationPrincipal Long createdBy,
            @RequestBody AutomationRuleCreateRequest request
    ) {
        AutomationRuleResponse response = AutomationRuleResponse.from(
                createAutomationRuleUseCase.create(request.toCommand(createdBy))
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                AutomationRuleResponseCode.CREATED,
                AutomationRuleResponseMessage.CREATED,
                response
        ));
    }
}
