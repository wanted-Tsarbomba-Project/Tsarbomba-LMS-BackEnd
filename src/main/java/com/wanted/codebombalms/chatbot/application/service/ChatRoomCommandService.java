package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.SendFirstMessageCommand;
import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import com.wanted.codebombalms.chatbot.application.result.RenameChatRoomResult;
import com.wanted.codebombalms.chatbot.application.usecase.ChatMessageCommandUseCase;
import com.wanted.codebombalms.chatbot.application.usecase.ChatRoomCommandUseCase;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatMessageRepository;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ChatRoomCommandService implements ChatRoomCommandUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomProvisionService roomProvisionService;
    private final ChatMessageCommandUseCase chatMessageCommandUseCase;

    @Override
    public Flux<AiChatStreamChunk> sendFirst(SendFirstMessageCommand command) {
        // 1) 방 준비(tx) — 동기로 먼저 끝낸다
        ChatRoom room = roomProvisionService.getOrCreate(command);

        // 2) room 이벤트를 맨 앞에 1회 → 이후 메시지 스트림 재중계
        return Flux.concat(
                Flux.just(new AiChatStreamChunk.Room(room.getId())),
                chatMessageCommandUseCase.send(
                        new SendMessageCommand(command.userId(), room.getId(), command.userMessage(), command.traceId()))
        );
    }

    @Override
    @Transactional
    public RenameChatRoomResult rename(Long roomId, Long userId, String title) {
        ChatRoom room = chatRoomRepository.getById(roomId);
        room.verifyOwner(userId);
        room.rename(title, Instant.now());
        ChatRoom saved = chatRoomRepository.save(room);
        return new RenameChatRoomResult(saved.getId(), saved.getTitle(), saved.getUpdatedAt());
    }

    @Override
    @Transactional
    public void delete(Long roomId, Long userId) {
        chatRoomRepository.getById(roomId).verifyOwner(userId);
        chatMessageRepository.deleteByRoomId(roomId);
        chatRoomRepository.deleteById(roomId);
    }
}
