//package com.wanted.codebombalms.domain.admin.automationrule.controller;
//
//import com.wanted.codebombalms.domain.admin.automationrule.dto.request.AutomationRuleCreateRequest;
//import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationTargetType;
//import com.wanted.codebombalms.domain.admin.automationrule.service.AutomationRuleService;
//import com.wanted.codebombalms.global.presentation.api.commonLegacy.ResponseDTO;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/admin/automation-rules")
//@RequiredArgsConstructor
//public class AutomationRuleController {
//
//    private final AutomationRuleService automationRuleService;
//
//    @GetMapping
//    public ResponseEntity<ResponseDTO> findAutomationRules(
//            @RequestParam(required = false) OperationTargetType targetType
//    ) {
//        return ResponseEntity.ok(new ResponseDTO(
//                HttpStatus.OK,
//                "자동화 규칙 목록 조회 성공",
//                automationRuleService.findAutomationRules(targetType)
//        ));
//    }
//
//    @GetMapping("/options")
//    public ResponseEntity<ResponseDTO> findAutomationRuleOptions(
//            @RequestParam(required = false) OperationTargetType targetType
//    ) {
//        return ResponseEntity.ok(new ResponseDTO(
//                HttpStatus.OK,
//                "자동화 규칙 옵션 조회 성공",
//                automationRuleService.findAutomationRuleOptions(targetType)
//        ));
//    }
//
//    @PostMapping
//    public ResponseEntity<ResponseDTO> createAutomationRule(
//            @RequestBody AutomationRuleCreateRequest request,
//            @AuthenticationPrincipal Long createdBy
//    ) {
//        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO(
//                HttpStatus.CREATED,
//                "자동화 규칙 등록 성공",
//                automationRuleService.createAutomationRule(request, createdBy)
//        ));
//    }
//}