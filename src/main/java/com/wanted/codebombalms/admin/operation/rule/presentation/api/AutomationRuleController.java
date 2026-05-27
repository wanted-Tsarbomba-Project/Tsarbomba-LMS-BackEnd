package com.wanted.codebombalms.admin.operation.rule.presentation.api;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.CreateAutomationRuleUseCase;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.GetAutomationRuleOptionsUseCase;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.GetAutomationRulesUseCase;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.UpdateAutomationRuleEnabledUseCase;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.UpdateAutomationRuleUseCase;
import com.wanted.codebombalms.admin.operation.rule.presentation.api.request.AutomationRuleCreateRequest;
import com.wanted.codebombalms.admin.operation.rule.presentation.api.request.AutomationRuleEnabledUpdateRequest;
import com.wanted.codebombalms.admin.operation.rule.presentation.api.request.AutomationRuleUpdateRequest;
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
    private final UpdateAutomationRuleUseCase updateAutomationRuleUseCase;
    private final UpdateAutomationRuleEnabledUseCase updateAutomationRuleEnabledUseCase;

    //규칙 목록 조회
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

    //규칙 옵션 조회
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

    // 규칙 등록
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

    // 규칙 threshold 일괄 수정
    @PatchMapping
    public ResponseEntity<ApiResponse<List<AutomationRuleResponse>>> updateAutomationRule(
            @RequestBody AutomationRuleUpdateRequest request
    ) {
        List<AutomationRuleResponse> response = updateAutomationRuleUseCase.update(request.toCommand())
                .stream()
                .map(AutomationRuleResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                AutomationRuleResponseCode.UPDATED,
                AutomationRuleResponseMessage.UPDATED,
                response
        ));
    }

    // 규칙 활성화 상태 수정
    @PatchMapping("/{automationRuleId}/enabled")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> updateAutomationRuleEnabled(
            @PathVariable Long automationRuleId,
            @RequestBody AutomationRuleEnabledUpdateRequest request
    ) {
        AutomationRuleResponse response = AutomationRuleResponse.from(
                updateAutomationRuleEnabledUseCase.updateEnabled(request.toCommand(automationRuleId))
        );

        return ResponseEntity.ok(ApiResponse.success(
                AutomationRuleResponseCode.UPDATED,
                AutomationRuleResponseMessage.UPDATED,
                response
        ));
    }


}
