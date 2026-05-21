package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;

public class ChatMessageMapper {

    private ChatMessageMapper() {}

    public static ChatMessage toDomain(ChatMessageJpaEntity entity) {
        return ChatMessage.restore(
                entity.getId(),
                entity.getRoomId(),
                entity.getRole(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }

    public static ChatMessageJpaEntity toEntity(ChatMessage domain) {
        return new ChatMessageJpaEntity(
                domain.getId(),
                domain.getRoomId(),
                domain.getRole(),
                domain.getContent(),
                domain.getCreatedAt()
        );
    }
}