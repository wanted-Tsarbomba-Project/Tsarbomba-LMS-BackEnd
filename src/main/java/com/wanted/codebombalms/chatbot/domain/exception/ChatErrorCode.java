package com.wanted.codebombalms.chatbot.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

    CHAT_ROOM_NOT_FOUND("CHT-001", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_FORBIDDEN("CHT-002", "채팅방에 접근 권한이 없습니다."),
    AI_RESPONSE_FAILED("CHT-003", "AI 응답 생성에 실패했습니다.");

    private final String code;
    private final String message;
}