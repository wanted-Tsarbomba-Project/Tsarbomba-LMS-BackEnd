package com.wanted.codebombalms.chatbot.application.usecase;

import com.wanted.codebombalms.chatbot.application.command.SendFirstMessageCommand;
import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import com.wanted.codebombalms.chatbot.application.result.RenameChatRoomResult;
import reactor.core.publisher.Flux;

public interface ChatRoomCommandUseCase {

    Flux<AiChatStreamChunk> sendFirst(SendFirstMessageCommand command);

    void delete(Long roomId, Long userId);

    RenameChatRoomResult rename(Long roomId, Long userId, String title);
}
