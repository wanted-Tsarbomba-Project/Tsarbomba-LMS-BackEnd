package com.wanted.codebombalms.chatbot.application.port;

import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import reactor.core.publisher.Flux;

public interface AiChatClient {

    /**
     * ChatContext를 FastAPI에 전송하고 AI 응답을 토큰 단위로 스트리밍한다.
     * Token(N회) → Done(1회) 순서이며, 도중 실패 시 Error로 끝난다.
     *
     * @param traceId 분산 추적용 상관 ID. FastAPI로 {@code X-Trace-Id} 헤더로 전파해
     *                두 서비스 로그를 같은 traceId로 엮는다. null 이면 전파하지 않는다.
     */
    Flux<AiChatStreamChunk> stream(ChatContext context, String traceId);
}
