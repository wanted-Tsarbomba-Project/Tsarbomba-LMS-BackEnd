package com.wanted.codebombalms.chatbot.application.port;

import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;

/**
 * AI 응답의 토큰 사용량을 관측 시스템에 기록하는 출력 포트.
 * application 은 이 인터페이스에만 의존하고, 구현(Micrometer 등)은 infrastructure 에 둔다.
 */
public interface RecordTokenUsagePort {

    void recordUsage(AiChatStreamChunk.TokenUsage usage);
}
