package com.wanted.codebombalms.serviceevent.presentation.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 관리자 운영 Q&A 챗봇 요청 (FE → Spring).
 * 대화 이력은 FE 가 세션 내에서 들고 있다가 매 요청에 실어 보낸다 (서버 무상태).
 */
public record OpsChatRequest(
        @NotBlank(message = "질문을 입력해주세요.")
        String userMessage,

        @Valid
        List<Message> conversationHistory
) {
    public record Message(
            @NotBlank String role,      // "user" or "ai"
            @NotBlank String content
    ) {
    }
}
