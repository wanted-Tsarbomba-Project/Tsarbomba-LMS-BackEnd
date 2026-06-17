package com.wanted.codebombalms.chatbot.application.port;

import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import reactor.core.publisher.Flux;

public interface AiChatClient {

    /**
     * ChatContext를 FastAPI에 전송하고 AI 응답을 토큰 단위로 스트리밍한다.
     * Token(N회) → Done(1회) 순서이며, 도중 실패 시 Error로 끝난다.
     */
    Flux<AiChatStreamChunk> stream(ChatContext context);
}
