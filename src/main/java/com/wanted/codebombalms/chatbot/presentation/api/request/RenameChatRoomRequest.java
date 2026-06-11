package com.wanted.codebombalms.chatbot.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameChatRoomRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 50, message = "제목은 최대 50자입니다.")
        String title
) {}
