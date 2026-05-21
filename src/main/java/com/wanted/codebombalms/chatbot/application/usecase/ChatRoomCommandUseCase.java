package com.wanted.codebombalms.chatbot.application.usecase;

import com.wanted.codebombalms.chatbot.application.command.CreateChatRoomCommand;
import com.wanted.codebombalms.chatbot.application.result.ChatRoomResult;

public interface ChatRoomCommandUseCase {

    // 채팅방 생성 또는 기존 방 반환
    ChatRoomResult create(CreateChatRoomCommand command);

    // 채팅방 삭제 (소유권 검증 포함)
    void delete(Long roomId, Long userId);
}
