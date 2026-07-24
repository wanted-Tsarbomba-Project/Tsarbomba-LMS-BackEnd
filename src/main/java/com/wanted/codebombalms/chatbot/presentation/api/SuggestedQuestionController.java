package com.wanted.codebombalms.chatbot.presentation.api;

import com.wanted.codebombalms.chatbot.application.model.SuggestedQuestionsResult;
import com.wanted.codebombalms.chatbot.application.usecase.GetSuggestedQuestionsUseCase;
import com.wanted.codebombalms.chatbot.presentation.api.response.SuggestedQuestionsResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 문제풀이방 진입 시 노출할 추천 질문(칩) 조회 API. 학생용(로그인만). */
@Tag(name = "Chatbot - 추천 질문", description = "문제별 자주 하는 질문 추천 조회 API")
@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Validated
public class SuggestedQuestionController {

    private final GetSuggestedQuestionsUseCase getSuggestedQuestionsUseCase;

    @Operation(
            summary = "문제별 추천 질문 조회",
            description = "해당 문제에서 자주 하는 질문 칩을 반환합니다. 생성 결과가 없으면 source=DEFAULT 기본칩."
    )
    @GetMapping("/suggested-questions")
    public ResponseEntity<ApiResponse<SuggestedQuestionsResponse>> getSuggestedQuestions(
            @RequestParam @Positive Long problemSetId,
            @RequestParam @Positive Long problemId
    ) {
        SuggestedQuestionsResult result = getSuggestedQuestionsUseCase.get(problemSetId, problemId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ChatResponseCode.SUGGESTED_QUESTIONS_RETRIEVED,
                        ChatResponseMessage.SUGGESTED_QUESTIONS_RETRIEVED,
                        SuggestedQuestionsResponse.of(problemSetId, problemId, result)
                )
        );
    }
}
