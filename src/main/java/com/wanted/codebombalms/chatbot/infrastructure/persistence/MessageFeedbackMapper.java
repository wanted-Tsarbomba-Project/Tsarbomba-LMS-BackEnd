package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import com.wanted.codebombalms.chatbot.domain.model.MessageFeedback;

public class MessageFeedbackMapper {

    private MessageFeedbackMapper() {}

    public static MessageFeedback toDomain(MessageFeedbackJpaEntity entity) {
        return MessageFeedback.restore(
                entity.getId(),
                entity.getMessageId(),
                entity.getUserId(),
                entity.getRating(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static MessageFeedbackJpaEntity toEntity(MessageFeedback domain) {
        return new MessageFeedbackJpaEntity(
                domain.getId(),
                domain.getMessageId(),
                domain.getUserId(),
                domain.getRating(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
