package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryAdapter implements ChatRoomRepository {

    private final SpringDataChatRoomRepository springDataRepository;

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        ChatRoomJpaEntity saved = springDataRepository.save(ChatRoomMapper.toEntity(chatRoom));
        return ChatRoomMapper.toDomain(saved);
    }

    @Override
    public Optional<ChatRoom> findById(Long id) {
        return springDataRepository.findById(id)
                .map(ChatRoomMapper::toDomain);
    }

    @Override
    public List<ChatRoom> findByUserId(Long userId) {
        return springDataRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(ChatRoomMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
}
