package com.wanted.codebombalms.chatbot.domain.model;

import java.time.Instant;

public class ChatMessage {

    private final Long id;
    private final Long roomId;
    private final MessageRole role;
    private final String content;
    private final Instant createdAt;

    private ChatMessage(Long id, Long roomId, MessageRole role, String content, Instant createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static ChatMessage createUserMessage(Long roomId, String content) {
        return new ChatMessage(null, roomId, MessageRole.USER, content, null);
    }

    public static ChatMessage createAiMessage(Long roomId, String content) {
        return new ChatMessage(null, roomId, MessageRole.AI, content, null);
    }

    public static ChatMessage restore(Long id, Long roomId, MessageRole role, String content, Instant createdAt) {
        return new ChatMessage(id, roomId, role, content, createdAt);
    }

    public Long getId() { return id; }
    public Long getRoomId() { return roomId; }
    public MessageRole getRole() { return role; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}