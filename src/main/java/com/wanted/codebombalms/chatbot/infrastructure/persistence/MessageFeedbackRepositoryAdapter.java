package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import com.wanted.codebombalms.chatbot.domain.model.MessageFeedback;
import com.wanted.codebombalms.chatbot.domain.repository.MessageFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MessageFeedbackRepositoryAdapter implements MessageFeedbackRepository {

    private final SpringDataMessageFeedbackRepository springDataRepository;

    @Override
    public MessageFeedback save(MessageFeedback feedback) {
        MessageFeedbackJpaEntity saved = springDataRepository.save(MessageFeedbackMapper.toEntity(feedback));
        return MessageFeedbackMapper.toDomain(saved);
    }

    @Override
    public Optional<MessageFeedback> findByMessageId(Long messageId) {
        return springDataRepository.findByMessageId(messageId)
                .map(MessageFeedbackMapper::toDomain);
    }

    @Override
    public List<MessageFeedback> findByMessageIdIn(List<Long> messageIds) {
        if (messageIds.isEmpty()) {
            return List.of();
        }
        return springDataRepository.findByMessageIdIn(messageIds)
                .stream()
                .map(MessageFeedbackMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByMessageId(Long messageId) {
        springDataRepository.deleteByMessageId(messageId);
    }
}
