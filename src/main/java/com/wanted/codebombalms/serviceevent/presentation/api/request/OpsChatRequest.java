package com.wanted.codebombalms.serviceevent.presentation.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 관리자 운영 Q&A 챗봇 요청 (FE → Spring).
 * 대화 이력은 FE 가 세션 내에서 들고 있다가 매 요청에 실어 보낸다 (서버 무상태).
 *
 * 길이 제한은 과도한 페이로드가 FastAPI/LLM 으로 흘러가 토큰 비용·지연이 폭증하는 것을 막는다.
 */
public record OpsChatRequest(
        @NotBlank(message = "질문을 입력해주세요.")
        @Size(max = 2000, message = "질문은 2000자 이내여야 합니다.")
        String userMessage,

        @Valid
        @Size(max = 40, message = "대화 이력은 최대 40개까지 전달할 수 있습니다.")
        List<Message> conversationHistory
) {
    public record Message(
            @NotBlank
            @Pattern(regexp = "user|ai", message = "role 은 user 또는 ai 여야 합니다.")
            String role,

            @NotBlank
            @Size(max = 4000, message = "메시지는 4000자 이내여야 합니다.")
            String content
    ) {
    }
}
