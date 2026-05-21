package com.wanted.codebombalms.chatbot.domain.repository;

import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;

import java.util.List;

public interface ChatMessageRepository {

    // 메시지 저장
    ChatMessage save(ChatMessage chatMessage);

    // roomId로 전체 메시지 조회
    List<ChatMessage> findByRoomId(Long roomId);

    // roomId 기준 최근 limit개 메시지 조회 (대화 히스토리용)
    List<ChatMessage> findRecentByRoomId(Long roomId, int limit);


}