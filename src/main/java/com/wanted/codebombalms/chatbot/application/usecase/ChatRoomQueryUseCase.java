package com.wanted.codebombalms.chatbot.application.usecase;

import com.wanted.codebombalms.chatbot.application.result.ChatRoomResult;

import java.util.List;
import java.util.Optional;

public interface ChatRoomQueryUseCase {

    // userId 기준 채팅방 목록 조회 (최신순)
    List<ChatRoomResult> listRooms(Long userId);

    // 문제 채팅방 단건 조회 → roomId (없으면 empty)
    Optional<Long> findProblemRoomId(Long userId, Long problemSetId, Long problemId);
}
