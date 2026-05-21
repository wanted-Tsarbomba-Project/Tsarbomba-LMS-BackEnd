package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.result.ChatMessageResult;
import com.wanted.codebombalms.chatbot.application.usecase.ChatMessageQueryUseCase;
import com.wanted.codebombalms.chatbot.domain.exception.ChatErrorCode;
import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;
import com.wanted.codebombalms.chatbot.domain.repository.ChatMessageRepository;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageQueryService implements ChatMessageQueryUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    public List<ChatMessageResult> listMessages(Long roomId, Long userId) {
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException(ChatErrorCode.CHAT_ROOM_NOT_FOUND))
                .verifyOwner(userId);

        return chatMessageRepository.findByRoomId(roomId)
                .stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    private ChatMessageResult toResult(ChatMessage message) {
        return new ChatMessageResult(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}