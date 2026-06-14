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
import com.wanted.codebombalms.chatbot.application.result.RenameChatRoomResult;
import java.time.Instant;
import java.util.Optional;

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
        ChatRoom room = getOrCreateRoom(command);

        AiChatResult aiResult = chatMessageCommandUseCase.send(
                new SendMessageCommand(command.userId(), room.getId(), command.userMessage())
        );

        return new SendFirstMessageResult(room.getId(), aiResult.answer());
    }

    private ChatRoom getOrCreateRoom(SendFirstMessageCommand command) {
        // 문제 채팅방(problemSetId+problemId 둘 다 있음)이면 기존 방 재사용
        if (command.problemSetId() != null && command.problemId() != null) {
            Optional<ChatRoom> existing = chatRoomRepository.findByUserIdAndProblem(
                    command.userId(), command.problemSetId(), command.problemId());
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        // 자유대화 방(null) 또는 신규 문제 방은 새로 생성
        return createRoom(
                command.userId(), command.problemSetId(), command.problemId(), command.userMessage());
    }

    private ChatRoom createRoom(Long userId, Long problemSetId, Long problemId, String userMessage) {
        String title = resolveTitle(problemSetId, problemId, userMessage);
        ChatRoom newRoom = ChatRoom.create(userId, problemSetId, problemId, title);
        return chatRoomRepository.save(newRoom);
    }

    private String resolveTitle(Long problemSetId, Long problemId, String userMessage) {
        if (problemSetId == null) {
            return summarize(userMessage);
        }

        ChatContextPort.ProblemSetInfo setInfo = chatContextPort.findProblemSet(problemSetId);
        String setTitle = setInfo != null ? setInfo.title() : "";

        if (problemId != null) {
            String problemTitle = chatContextPort.findProblemTitle(problemId);
            if (problemTitle != null && !problemTitle.isBlank()) {
                return setTitle + " - " + problemTitle;
            }
        }
        return setTitle;
    }

    private String summarize(String message) {
        if (message == null || message.isBlank()) {
            return "새 대화";
        }
        String trimmed = message.strip();
        return trimmed.length() <= 20 ? trimmed : trimmed.substring(0, 20) + "…";
    }

    @Override
    public RenameChatRoomResult rename(Long roomId, Long userId, String title) {
        ChatRoom room = chatRoomRepository.getById(roomId);
        room.verifyOwner(userId);
        room.rename(title, Instant.now());
        ChatRoom saved = chatRoomRepository.save(room);
        return new RenameChatRoomResult(saved.getId(), saved.getTitle(), saved.getUpdatedAt());
    }

    @Override
    public void delete(Long roomId, Long userId) {
        chatRoomRepository.getById(roomId).verifyOwner(userId);
        chatMessageRepository.deleteByRoomId(roomId);
        chatRoomRepository.deleteById(roomId);
    }
}
