package com.wanted.codebombalms.chatbot.application.command;

import com.wanted.codebombalms.chatbot.domain.model.FeedbackRating;

public record SaveFeedbackCommand(
        Long userId,
        Long messageId,
        FeedbackRating rating
) {}
