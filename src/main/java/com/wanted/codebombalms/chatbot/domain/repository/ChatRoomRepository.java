package com.wanted.codebombalms.chatbot.domain.repository;

import com.wanted.codebombalms.chatbot.domain.exception.ChatErrorCode;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository {

    // 채팅방 저장 (생성/수정)
    ChatRoom save(ChatRoom chatRoom);

    // id로 채팅방 단건 조회
    Optional<ChatRoom> findById(Long id);

    // id로 채팅방 조회, 없으면 NotFoundException
    default ChatRoom getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    // 문제 채팅방 단건 조회 (userId + problemSetId + problemId)
    Optional<ChatRoom> findByUserIdAndProblem(Long userId, Long problemSetId, Long problemId);

    // userId로 채팅방 목록 조회
    List<ChatRoom> findByUserId(Long userId);

    // roomId로 채팅방 삭제
    void deleteById(Long id);
}
