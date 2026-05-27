package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, Long> {

    // userId로 채팅방 목록 조회 (최신순)
    List<ChatRoomJpaEntity> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
