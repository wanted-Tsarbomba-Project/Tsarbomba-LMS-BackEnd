package com.wanted.codebombalms.domain.admin.operation.rule.presentation.api;

import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.domain.admin.operation.rule.application.usecase.CreateAutomationRuleUseCase;
import com.wanted.codebombalms.domain.admin.operation.rule.application.usecase.GetAutomationRuleOptionsUseCase;
import com.wanted.codebombalms.domain.admin.operation.rule.application.usecase.GetAutomationRulesUseCase;
import com.wanted.codebombalms.domain.admin.operation.rule.presentation.api.request.AutomationRuleCreateRequest;
import com.wanted.codebombalms.domain.admin.operation.rule.presentation.api.response.AutomationRuleOptionsResponse;
import com.wanted.codebombalms.domain.admin.operation.rule.presentation.api.response.AutomationRuleResponse;
import com.wanted.codebombalms.global.presentation.api.commonLegacy.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/automation-rules")
@RequiredArgsConstructor
public class AutomationRuleController {

    private final GetAutomationRulesUseCase getAutomationRulesUseCase;
    private final GetAutomationRuleOptionsUseCase getAutomationRuleOptionsUseCase;
    private final CreateAutomationRuleUseCase createAutomationRuleUseCase;

    @GetMapping
    public ResponseEntity<ResponseDTO> findAutomationRules(
            @RequestParam(required = false) OperationTargetType targetType
    ) {
        var response = getAutomationRulesUseCase.getRules(targetType)
                .stream()
                .map(AutomationRuleResponse::from)
                .toList();

        return ResponseEntity.ok(new ResponseDTO(
                HttpStatus.OK,
                "자동화 규칙 목록 조회 성공",
                response
        ));
    }

    @GetMapping("/options")
    public ResponseEntity<ResponseDTO> findAutomationRuleOptions() {
        var response = AutomationRuleOptionsResponse.from(
                getAutomationRuleOptionsUseCase.getOptions()
        );

        return ResponseEntity.ok(new ResponseDTO(
                HttpStatus.OK,
                "자동화 규칙 옵션 조회 성공",
                response
        ));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> createAutomationRule(
            @AuthenticationPrincipal Long createdBy,
            @RequestBody AutomationRuleCreateRequest request
    ) {
        var response = AutomationRuleResponse.from(
                createAutomationRuleUseCase.create(request.toCommand(createdBy))
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO(
                HttpStatus.CREATED,
                "자동화 규칙 등록 성공",
                response
        ));
    }
}
