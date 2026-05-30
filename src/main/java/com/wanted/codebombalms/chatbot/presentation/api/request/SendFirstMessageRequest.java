package com.wanted.codebombalms.chatbot.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

public record SendFirstMessageRequest(
        @NotBlank(message = "메시지를 입력해주세요.")  // ← 추가
        String userMessage,
        Long problemSetId,   // null 허용 (선택값)
        Long problemId       // null 허용 (선택값)
) {}
