package com.wanted.codebombalms.chatbot.presentation.api;

import com.wanted.codebombalms.chatbot.application.usecase.GenerateSuggestedQuestionsUseCase;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 추천 질문 생성 배치 수동 트리거 (운영용). 스케줄러와 같은 UseCase 를 즉시 호출한다.
 * 무거운 운영 작업이라 ADMIN 전용. (권한식은 프로젝트 보안 설정에 맞춰 조정 가능)
 */
@Tag(name = "Chatbot - 추천 질문(운영)", description = "추천 질문 생성 배치 수동 트리거 (ADMIN)")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/admin/suggested-questions")
@RequiredArgsConstructor
public class SuggestedQuestionAdminController {

    private final GenerateSuggestedQuestionsUseCase generateSuggestedQuestionsUseCase;

    @Operation(
            summary = "추천 질문 생성 배치 수동 실행",
            description = "전체 문제를 스캔해 임계값을 넘는 문제의 추천 질문을 즉시 생성·저장합니다."
    )
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GenerateResult>> generate() {
        int savedProblems = generateSuggestedQuestionsUseCase.generate();

        return ResponseEntity.ok(
                ApiResponse.success(
                        ChatResponseCode.SUGGESTED_QUESTIONS_GENERATED,
                        ChatResponseMessage.SUGGESTED_QUESTIONS_GENERATED,
                        new GenerateResult(savedProblems)
                )
        );
    }

    public record GenerateResult(int savedProblems) {
    }
}
