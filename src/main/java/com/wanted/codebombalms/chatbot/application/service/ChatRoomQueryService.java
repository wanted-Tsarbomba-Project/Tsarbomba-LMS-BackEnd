package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.result.ChatRoomResult;
import com.wanted.codebombalms.chatbot.application.usecase.ChatRoomQueryUseCase;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryService implements ChatRoomQueryUseCase {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public List<ChatRoomResult> listRooms(Long userId) {
        return chatRoomRepository.findByUserId(userId)
                .stream()
                .map(this::toResult)
                .collect(Collectors.toList());
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
}