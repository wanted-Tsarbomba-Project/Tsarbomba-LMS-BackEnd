package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.CreateChatRoomCommand;
import com.wanted.codebombalms.chatbot.application.result.ChatRoomResult;
import com.wanted.codebombalms.chatbot.application.usecase.CreateChatRoomUseCase;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomCommandService implements CreateChatRoomUseCase {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public ChatRoomResult handle(CreateChatRoomCommand command) {
        return chatRoomRepository
                .findByUserIdAndProblemSetId(command.userId(), command.problemSetId())
                .map(this::toResult)
                .orElseGet(() -> createNewRoom(command));
    }

    private ChatRoomResult createNewRoom(CreateChatRoomCommand command) {
        ChatRoom newRoom = ChatRoom.create(command.userId(), command.problemSetId());
        ChatRoom saved = chatRoomRepository.save(newRoom);
        return toResult(saved);
    }

    private ChatRoomResult toResult(ChatRoom chatRoom) {
        return new ChatRoomResult(
                chatRoom.getId(),
                chatRoom.getProblemSetId(),
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt()
        );
    }
}