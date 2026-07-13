package com.wanted.codebombalms.chatbot.domain.model;

import java.time.Instant;

public class MessageFeedback {

    private final Long id;
    private final Long messageId;
    private final Long userId;
    private FeedbackRating rating;
    private final Instant createdAt;
    private final Instant updatedAt;

    private MessageFeedback(
            Long id,
            Long messageId,
            Long userId,
            FeedbackRating rating,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.messageId = messageId;
        this.userId = userId;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MessageFeedback create(Long messageId, Long userId, FeedbackRating rating) {
        return new MessageFeedback(null, messageId, userId, rating, null, null);
    }

    public static MessageFeedback restore(
            Long id,
            Long messageId,
            Long userId,
            FeedbackRating rating,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new MessageFeedback(id, messageId, userId, rating, createdAt, updatedAt);
    }

    // 이미 존재하는 평가의 값을 전환한다(UP↔DOWN). updatedAt 은 영속 계층(@PreUpdate)에서 갱신.
    public void changeRating(FeedbackRating newRating) {
        this.rating = newRating;
    }

    public Long getId() { return id; }
    public Long getMessageId() { return messageId; }
    public Long getUserId() { return userId; }
    public FeedbackRating getRating() { return rating; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
