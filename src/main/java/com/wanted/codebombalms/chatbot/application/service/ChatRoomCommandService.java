package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.SendFirstMessageCommand;
import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.chatbot.application.result.AiChatResult;
import com.wanted.codebombalms.chatbot.application.result.SendFirstMessageResult;
import com.wanted.codebombalms.chatbot.application.usecase.ChatMessageCommandUseCase;
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
    private final ChatMessageCommandUseCase chatMessageCommandUseCase;

    @Override
    public SendFirstMessageResult sendFirst(SendFirstMessageCommand command) {
        ChatRoom room = createRoom(command.userId(), command.problemSetId(), command.problemId());

        AiChatResult aiResult = chatMessageCommandUseCase.send(
                new SendMessageCommand(command.userId(), room.getId(), command.userMessage())
        );

        return new SendFirstMessageResult(room.getId(), aiResult.answer());
    }

    private ChatRoom createRoom(Long userId, Long problemSetId, Long problemId) {
        ChatContextPort.ProblemSetInfo problemSetInfo =
                problemSetId != null ? chatContextPort.findProblemSet(problemSetId) : null;

        String title = problemSetInfo != null ? problemSetInfo.title() : "";
        ChatRoom newRoom = ChatRoom.create(userId, problemSetId, problemId, title);
        return chatRoomRepository.save(newRoom);
    }

    @Override
    public void delete(Long roomId, Long userId) {
        chatRoomRepository.getById(roomId).verifyOwner(userId);
        chatMessageRepository.deleteByRoomId(roomId);
        chatRoomRepository.deleteById(roomId);
    }
}
