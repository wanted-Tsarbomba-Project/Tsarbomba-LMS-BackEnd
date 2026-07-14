package com.wanted.codebombalms.chatbot.presentation.api.response;

import com.wanted.codebombalms.chatbot.domain.model.FeedbackRating;
import com.wanted.codebombalms.chatbot.domain.model.MessageFeedback;

public record MessageFeedbackResponse(
        Long messageId,
        FeedbackRating rating
) {
    public static MessageFeedbackResponse from(MessageFeedback feedback) {
        return new MessageFeedbackResponse(
                feedback.getMessageId(),
                feedback.getRating()
        );
    }
}
