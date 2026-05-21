package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.AiChatClient;
import com.wanted.codebombalms.chatbot.application.result.AiChatResult;
import com.wanted.codebombalms.chatbot.application.usecase.ChatMessageCommandUseCase;
import com.wanted.codebombalms.chatbot.domain.exception.ChatErrorCode;
import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatMessageRepository;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageCommandService implements ChatMessageCommandUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatContextBuilder chatContextBuilder;
    private final AiChatClient aiChatClient;

    @Override
    public AiChatResult send(SendMessageCommand command) {
        ChatRoom chatRoom = chatRoomRepository.findById(command.roomId())
                .orElseThrow(() -> new NotFoundException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        chatRoom.verifyOwner(command.userId());

        chatMessageRepository.save(
                ChatMessage.createUserMessage(command.roomId(), command.userMessage())
        );

        ChatContext context = chatContextBuilder.build(command, chatRoom);

        AiChatClient.AiChatClientResponse aiResponse = aiChatClient.call(context);

        chatMessageRepository.save(
                ChatMessage.createAiMessage(command.roomId(), aiResponse.answer())
        );

        chatRoom.updateTimestamp(Instant.now());
        chatRoomRepository.save(chatRoom);

        return new AiChatResult(aiResponse.answer());
    }
}