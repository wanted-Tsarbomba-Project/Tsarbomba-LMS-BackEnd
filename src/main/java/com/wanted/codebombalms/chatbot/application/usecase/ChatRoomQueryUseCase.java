package com.wanted.codebombalms.chatbot.application.usecase;

import com.wanted.codebombalms.chatbot.application.result.ChatRoomResult;

import java.util.List;

public interface ChatRoomQueryUseCase {

    // userId 기준 채팅방 목록 조회 (최신순)
    List<ChatRoomResult> listRooms(Long userId);
}