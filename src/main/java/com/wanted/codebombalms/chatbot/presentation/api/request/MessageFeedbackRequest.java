package com.wanted.codebombalms.chatbot.presentation.api.request;

import com.wanted.codebombalms.chatbot.domain.model.FeedbackRating;
import jakarta.validation.constraints.NotNull;

public record MessageFeedbackRequest(
        @NotNull(message = "평가 값(UP/DOWN)을 입력해주세요.")
        FeedbackRating rating
) {}
