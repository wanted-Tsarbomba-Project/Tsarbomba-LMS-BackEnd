package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataChatMessageRepository extends JpaRepository<ChatMessageJpaEntity, Long> {

    // roomId로 전체 메시지 조회 (시간순)
    List<ChatMessageJpaEntity> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    // roomId 기준 최근 limit개 메시지 조회 (최신순 → 역순 반환)
    @Query("SELECT m FROM ChatMessageJpaEntity m WHERE m.roomId = :roomId ORDER BY m.createdAt DESC LIMIT :limit")
    List<ChatMessageJpaEntity> findRecentByRoomId(@Param("roomId") Long roomId, @Param("limit") int limit);

    // roomId로 메시지 전체 삭제
    void deleteByRoomId(Long roomId);

}
