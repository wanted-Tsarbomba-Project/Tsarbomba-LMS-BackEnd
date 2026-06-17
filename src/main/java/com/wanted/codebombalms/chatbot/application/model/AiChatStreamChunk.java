package com.wanted.codebombalms.chatbot.application.model;

/**
 * AI 응답 스트림의 한 조각. FastAPI SSE 프레임 ↔ 브라우저 SSE 프레임 사이의 도메인 표현이다.
 *  - Room  : 첫 메시지에서 생성/조회된 방 ID (맨 앞 1회, 도메인 추가 이벤트)
 *  - Token : 본문 토큰 (N회)
 *  - Done  : 정상 종료 + 토큰 사용량 (1회)
 *  - Error : 스트림 중 에러 (도메인 에러코드)
 */
public sealed interface AiChatStreamChunk
        permits AiChatStreamChunk.Room, AiChatStreamChunk.Token, AiChatStreamChunk.Done, AiChatStreamChunk.Error {

    record Room(Long roomId) implements AiChatStreamChunk {}

    record Token(String text) implements AiChatStreamChunk {}

    record Done(TokenUsage usage) implements AiChatStreamChunk {}

    record Error(String code, String message) implements AiChatStreamChunk {}

    record TokenUsage(int promptTokens, int completionTokens, int totalTokens) {}
}
