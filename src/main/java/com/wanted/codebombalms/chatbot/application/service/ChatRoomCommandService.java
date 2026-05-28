package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.CreateChatRoomCommand;
import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.chatbot.application.result.ChatRoomResult;
import com.wanted.codebombalms.chatbot.application.usecase.ChatRoomCommandUseCase;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatMessageRepository;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomCommandService implements ChatRoomCommandUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatContextPort chatContextPort;

    @Override
    public ChatRoomResult create(CreateChatRoomCommand command) {
        ChatContextPort.ProblemSetInfo problemSetInfo =
                chatContextPort.findProblemSet(command.problemSetId());

        String title = problemSetInfo != null ? problemSetInfo.title() : "";
        ChatRoom newRoom = ChatRoom.create(command.userId(), command.problemSetId(), command.problemId(), title);
        ChatRoom saved = chatRoomRepository.save(newRoom);
        return toResult(saved);
    }

    private ChatRoomResult toResult(ChatRoom chatRoom) {
        return new ChatRoomResult(
                chatRoom.getId(),
                chatRoom.getProblemSetId(),
                chatRoom.getProblemId(),
                chatRoom.getTitle(),
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt()
        );
    }

    @Override
    public void delete(Long roomId, Long userId) {
        chatRoomRepository.getById(roomId).verifyOwner(userId);
        chatMessageRepository.deleteByRoomId(roomId);
        chatRoomRepository.deleteById(roomId);
    }
}
