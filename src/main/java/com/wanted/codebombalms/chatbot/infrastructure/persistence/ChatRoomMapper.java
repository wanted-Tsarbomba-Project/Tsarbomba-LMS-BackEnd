package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;

public class ChatRoomMapper {

    private ChatRoomMapper() {}

    public static ChatRoom toDomain(ChatRoomJpaEntity entity) {
        return ChatRoom.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getProblemSetId(),
                entity.getProblemId(),
                entity.getTitle(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ChatRoomJpaEntity toEntity(ChatRoom domain) {
        return new ChatRoomJpaEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getProblemSetId(),
                domain.getProblemId(),
                domain.getTitle(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
