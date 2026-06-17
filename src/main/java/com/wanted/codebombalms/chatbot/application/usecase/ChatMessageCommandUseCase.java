package com.wanted.codebombalms.chatbot.application.usecase;

import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import reactor.core.publisher.Flux;

public interface ChatMessageCommandUseCase {

    Flux<AiChatStreamChunk> send(SendMessageCommand command);
}
