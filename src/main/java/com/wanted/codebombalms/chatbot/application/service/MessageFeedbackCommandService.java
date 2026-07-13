package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.SaveFeedbackCommand;
import com.wanted.codebombalms.chatbot.application.usecase.MessageFeedbackCommandUseCase;
import com.wanted.codebombalms.chatbot.domain.exception.ChatErrorCode;
import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;
import com.wanted.codebombalms.chatbot.domain.model.MessageFeedback;
import com.wanted.codebombalms.chatbot.domain.model.MessageRole;
import com.wanted.codebombalms.chatbot.domain.repository.ChatMessageRepository;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import com.wanted.codebombalms.chatbot.domain.repository.MessageFeedbackRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageFeedbackCommandService implements MessageFeedbackCommandUseCase {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageFeedbackRepository messageFeedbackRepository;

    @Override
    public MessageFeedback save(SaveFeedbackCommand command) {
        ChatMessage message = chatMessageRepository.getById(command.messageId());
        if (message.getRole() != MessageRole.AI) {
            throw new ValidationException(ChatErrorCode.FEEDBACK_TARGET_NOT_AI);
        }
        chatRoomRepository.getById(message.getRoomId()).verifyOwner(command.userId());

        MessageFeedback feedback = messageFeedbackRepository.findByMessageId(command.messageId())
                .map(existing -> {
                    existing.changeRating(command.rating());
                    return existing;
                })
                .orElseGet(() -> MessageFeedback.create(
                        command.messageId(), command.userId(), command.rating()));

        return messageFeedbackRepository.save(feedback);
    }

    @Override
    public void delete(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.getById(messageId);
        chatRoomRepository.getById(message.getRoomId()).verifyOwner(userId);

        messageFeedbackRepository.deleteByMessageId(messageId);
    }
}
