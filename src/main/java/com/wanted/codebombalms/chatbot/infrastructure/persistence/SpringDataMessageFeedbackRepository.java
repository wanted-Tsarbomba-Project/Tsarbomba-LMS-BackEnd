package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataMessageFeedbackRepository extends JpaRepository<MessageFeedbackJpaEntity, Long> {

    Optional<MessageFeedbackJpaEntity> findByMessageId(Long messageId);

    List<MessageFeedbackJpaEntity> findByMessageIdIn(List<Long> messageIds);

    void deleteByMessageId(Long messageId);
}
