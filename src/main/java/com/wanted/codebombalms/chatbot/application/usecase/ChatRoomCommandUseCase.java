package com.wanted.codebombalms.chatbot.application.usecase;

import com.wanted.codebombalms.chatbot.application.command.CreateChatRoomCommand;
import com.wanted.codebombalms.chatbot.application.result.ChatRoomResult;

public interface ChatRoomCommandUseCase {

    // 채팅방 생성 또는 기존 방 반환
    ChatRoomResult create(CreateChatRoomCommand command);
}
