package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.CreateChatRoomCommand;
import com.wanted.codebombalms.chatbot.application.result.ChatRoomResult;
import com.wanted.codebombalms.chatbot.application.usecase.ChatRoomCommandUseCase;
import com.wanted.codebombalms.chatbot.domain.exception.ChatErrorCode;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomCommandService implements ChatRoomCommandUseCase {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public ChatRoomResult create(CreateChatRoomCommand command) {
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
                null,
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt()
        );
    }

    @Override
    public void delete(Long roomId, Long userId) {
        chatRoomRepository.getById(roomId).verifyOwner(userId);
        chatRoomRepository.deleteById(roomId);
    }

}