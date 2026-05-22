package com.wanted.codebombalms.chatbot.application.usecase;

import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.result.AiChatResult;

public interface ChatMessageCommandUseCase {

    // 유저 메시지 저장 + FastAPI 호출 + AI 응답 저장 후 반환
    AiChatResult send(SendMessageCommand command);
}