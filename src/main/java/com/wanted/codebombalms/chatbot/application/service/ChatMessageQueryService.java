package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.result.ChatMessageResult;
import com.wanted.codebombalms.chatbot.application.usecase.ChatMessageQueryUseCase;
import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;
import com.wanted.codebombalms.chatbot.domain.model.FeedbackRating;
import com.wanted.codebombalms.chatbot.domain.model.MessageFeedback;
import com.wanted.codebombalms.chatbot.domain.repository.ChatMessageRepository;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import com.wanted.codebombalms.chatbot.domain.repository.MessageFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageQueryService implements ChatMessageQueryUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MessageFeedbackRepository messageFeedbackRepository;

    @Override
    public List<ChatMessageResult> listMessages(Long roomId, Long userId) {
        chatRoomRepository.getById(roomId).verifyOwner(userId);

        List<ChatMessage> messages = chatMessageRepository.findByRoomId(roomId);

        List<Long> messageIds = messages.stream()
                .map(ChatMessage::getId)
                .collect(Collectors.toList());
        Map<Long, FeedbackRating> feedbackByMessageId = messageFeedbackRepository.findByMessageIdIn(messageIds)
                .stream()
                .collect(Collectors.toMap(MessageFeedback::getMessageId, MessageFeedback::getRating));

        return messages.stream()
                .map(message -> toResult(message, feedbackByMessageId.get(message.getId())))
                .collect(Collectors.toList());
    }

    private ChatMessageResult toResult(ChatMessage message, FeedbackRating feedback) {
        return new ChatMessageResult(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt(),
                feedback
        );
    }
}
