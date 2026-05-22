package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;
import com.wanted.codebombalms.chatbot.domain.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryAdapter implements ChatMessageRepository {

    private final SpringDataChatMessageRepository springDataRepository;

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        ChatMessageJpaEntity saved = springDataRepository.save(ChatMessageMapper.toEntity(chatMessage));
        return ChatMessageMapper.toDomain(saved);
    }

    @Override
    public List<ChatMessage> findByRoomId(Long roomId) {
        return springDataRepository.findByRoomIdOrderByCreatedAtAsc(roomId)
                .stream()
                .map(ChatMessageMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessage> findRecentByRoomId(Long roomId, int limit) {
        return springDataRepository.findRecentByRoomId(roomId, limit)
                .stream()
                .map(ChatMessageMapper::toDomain)
                .collect(Collectors.toList());
    }
}