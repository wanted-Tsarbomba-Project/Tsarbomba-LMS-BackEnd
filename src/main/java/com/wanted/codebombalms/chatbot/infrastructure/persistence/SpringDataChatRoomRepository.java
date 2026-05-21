package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, Long> {

    // userId + problemSetId로 채팅방 조회
    Optional<ChatRoomJpaEntity> findByUserIdAndProblemSetId(Long userId, Long problemSetId);

    // userId로 채팅방 목록 조회 (최신순)
    java.util.List<ChatRoomJpaEntity> findByUserIdOrderByUpdatedAtDesc(Long userId);
}