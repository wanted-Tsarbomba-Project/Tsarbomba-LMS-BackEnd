package com.wanted.codebombalms.chatbot.domain.repository;

import com.wanted.codebombalms.chatbot.domain.exception.ChatErrorCode;
import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository {

    // 메시지 저장
    ChatMessage save(ChatMessage chatMessage);

    // messageId 로 메시지 단건 조회
    Optional<ChatMessage> findById(Long messageId);

    // messageId 로 메시지 조회, 없으면 NotFoundException
    default ChatMessage getById(Long messageId) {
        return findById(messageId)
                .orElseThrow(() -> new NotFoundException(ChatErrorCode.MESSAGE_NOT_FOUND));
    }

    // roomId로 전체 메시지 조회
    List<ChatMessage> findByRoomId(Long roomId);

    // roomId 기준 최근 limit개 메시지 조회 (대화 히스토리용)
    List<ChatMessage> findRecentByRoomId(Long roomId, int limit);

    // roomId로 메시지 전체 삭제
    void deleteByRoomId(Long roomId);
}
