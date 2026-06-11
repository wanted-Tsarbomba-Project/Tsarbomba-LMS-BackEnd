package com.wanted.codebombalms.chatbot.domain.model;

import com.wanted.codebombalms.chatbot.domain.exception.ChatErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;

import java.time.Instant;

public class ChatRoom {

    private final Long id;
    private final Long userId;
    private final Long problemSetId;
    private final Long problemId;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;

    private ChatRoom(Long id, Long userId, Long problemSetId, Long problemId, String title,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.problemSetId = problemSetId;
        this.problemId = problemId;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ChatRoom create(Long userId, Long problemSetId, Long problemId, String title) {
        return new ChatRoom(null, userId, problemSetId, problemId, title, null, null);
    }

    public static ChatRoom restore(Long id, Long userId, Long problemSetId, Long problemId, String title,
                                   Instant createdAt, Instant updatedAt) {
        return new ChatRoom(id, userId, problemSetId, problemId, title, createdAt, updatedAt);
    }

    public void verifyOwner(Long requestUserId) {
        if (!this.userId.equals(requestUserId)) {
            throw new ForbiddenException(ChatErrorCode.CHAT_ROOM_FORBIDDEN);
        }
    }

    public void updateTimestamp(Instant now) {
        this.updatedAt = now;
    }

    public void rename(String newTitle, Instant now) {
        this.title = newTitle;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getProblemSetId() { return problemSetId; }
    public Long getProblemId() { return problemId; }
    public String getTitle() { return title; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
