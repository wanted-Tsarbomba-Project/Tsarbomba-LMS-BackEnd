package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.SendFirstMessageCommand;
import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 첫 메시지의 방 준비(get-or-create)를 담는 트랜잭션 경계.
 * sendFirst(스트리밍 오케스트레이터)에서 호출되므로 별도 빈으로 둬야 @Transactional이 적용된다.
 */
@Service
@RequiredArgsConstructor
public class ChatRoomProvisionService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatContextPort chatContextPort;

    @Transactional
    public ChatRoom getOrCreate(SendFirstMessageCommand command) {
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
}
