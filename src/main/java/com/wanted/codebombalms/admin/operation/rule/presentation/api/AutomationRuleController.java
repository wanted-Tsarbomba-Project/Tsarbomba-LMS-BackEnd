package com.wanted.codebombalms.admin.operation.rule.presentation.api;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.GetAutomationRulesUseCase;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.UpdateAutomationRuleEnabledUseCase;
import com.wanted.codebombalms.admin.operation.rule.application.usecase.UpdateAutomationRuleUseCase;
import com.wanted.codebombalms.admin.operation.rule.presentation.api.request.AutomationRuleEnabledUpdateRequest;
import com.wanted.codebombalms.admin.operation.rule.presentation.api.request.AutomationRuleUpdateRequest;
import com.wanted.codebombalms.admin.operation.rule.presentation.api.response.AutomationRuleResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/automation-rules")
@RequiredArgsConstructor
public class AutomationRuleController {

    private final GetAutomationRulesUseCase getAutomationRulesUseCase;
    private final UpdateAutomationRuleUseCase updateAutomationRuleUseCase;
    private final UpdateAutomationRuleEnabledUseCase updateAutomationRuleEnabledUseCase;

    //규칙 목록 조회
    @Operation(summary = "자동화 규칙 목록 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
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

    // 규칙 threshold 일괄 수정
    @Operation(summary = "자동화 규칙 threshold 일괄 수정")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ADM-ARL-005: 자동화 규칙 수정 요청이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ADM-ARL-002: 임계값이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ADM-ARL-003: 최소 표본 수가 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "ADM-ARL-006: 자동화 규칙을 찾을 수 없습니다.")
    })
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
    @Operation(summary = "자동화 규칙 활성화 상태 수정")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ADM-ARL-007: 자동화 규칙 활성화 상태 변경 요청이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "ADM-ARL-006: 자동화 규칙을 찾을 수 없습니다.")
    })
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
