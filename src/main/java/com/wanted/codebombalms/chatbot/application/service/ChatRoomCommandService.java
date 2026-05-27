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
        ChatContextPort.CurrentProblemInfo info =
                chatContextPort.findCurrentProblemInfo(command.userId(), command.problemSetId());

        String title = buildTitle(info);
        ChatRoom newRoom = ChatRoom.create(command.userId(), command.problemSetId(), info.problemId(), title);
        ChatRoom saved = chatRoomRepository.save(newRoom);
        return toResult(saved);
    }

    private String buildTitle(ChatContextPort.CurrentProblemInfo info) {
        String setTitle = info.problemSetTitle() != null ? info.problemSetTitle() : "";
        String problemTitle = info.problemTitle() != null ? info.problemTitle() : "";
        return setTitle + " - " + problemTitle;
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
