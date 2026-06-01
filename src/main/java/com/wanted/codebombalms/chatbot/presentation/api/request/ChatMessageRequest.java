package com.wanted.codebombalms.chatbot.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank(message = "메시지를 입력해주세요.")  // ← 추가
        String userMessage
) {}
