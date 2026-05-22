package com.wanted.codebombalms.chatbot.application.usecase;

import com.wanted.codebombalms.chatbot.application.result.ChatMessageResult;

import java.util.List;

public interface ChatMessageQueryUseCase {

    // roomId 기준 채팅 내역 조회 (소유권 검증 포함)
    List<ChatMessageResult> listMessages(Long roomId, Long userId);
}