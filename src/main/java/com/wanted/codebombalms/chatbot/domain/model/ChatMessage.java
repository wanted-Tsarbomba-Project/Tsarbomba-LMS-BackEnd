package com.wanted.codebombalms.chatbot.domain.model;

import java.time.Instant;

public class ChatMessage {

    private final Long id;
    private final Long roomId;
    private final MessageRole role;
    private final String content;
    // 토큰 사용량: AI 응답 행에만 값이 있고 USER 행은 null 이다.
    private final Integer promptTokens;
    private final Integer completionTokens;
    private final Integer totalTokens;
    private final Instant createdAt;

    private ChatMessage(
            Long id,
            Long roomId,
            MessageRole role,
            String content,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens,
            Instant createdAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.role = role;
        this.content = content;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.createdAt = createdAt;
    }

    public static ChatMessage createUserMessage(Long roomId, String content) {
        return new ChatMessage(null, roomId, MessageRole.USER, content, null, null, null, null);
    }

    public static ChatMessage createAiMessage(
            Long roomId,
            String content,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens
    ) {
        return new ChatMessage(
                null, roomId, MessageRole.AI, content,
                promptTokens, completionTokens, totalTokens, null
        );
    }

    public static ChatMessage restore(
            Long id,
            Long roomId,
            MessageRole role,
            String content,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens,
            Instant createdAt
    ) {
        return new ChatMessage(
                id, roomId, role, content,
                promptTokens, completionTokens, totalTokens, createdAt
        );
    }

    public Long getId() { return id; }
    public Long getRoomId() { return roomId; }
    public MessageRole getRole() { return role; }
    public String getContent() { return content; }
    public Integer getPromptTokens() { return promptTokens; }
    public Integer getCompletionTokens() { return completionTokens; }
    public Integer getTotalTokens() { return totalTokens; }
    public Instant getCreatedAt() { return createdAt; }
}