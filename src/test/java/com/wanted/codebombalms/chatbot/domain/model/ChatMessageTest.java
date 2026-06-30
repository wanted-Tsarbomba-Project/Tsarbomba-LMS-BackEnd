package com.wanted.codebombalms.chatbot.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("ChatMessage 도메인")
class ChatMessageTest {

    @Test
    @DisplayName("AI 메시지 토큰 사용량이 음수면 생성 시 검증 예외가 발생한다")
    void AI메시지_음수토큰_거부() {
        assertThatThrownBy(() -> ChatMessage.createAiMessage(10L, "답변", -1, 0, 0))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("AI 메시지 토큰 사용량이 0 이상이면 정상 생성된다")
    void AI메시지_정상토큰_생성() {
        assertThatCode(() -> ChatMessage.createAiMessage(10L, "답변", 10, 2, 12))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("USER 메시지는 토큰 값이 모두 null 이다")
    void USER메시지_토큰_null() {
        ChatMessage userMessage = ChatMessage.createUserMessage(10L, "질문");

        assertThat(userMessage.getPromptTokens()).isNull();
        assertThat(userMessage.getCompletionTokens()).isNull();
        assertThat(userMessage.getTotalTokens()).isNull();
    }
}
