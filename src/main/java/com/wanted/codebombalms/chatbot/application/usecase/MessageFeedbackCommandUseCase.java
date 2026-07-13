package com.wanted.codebombalms.chatbot.application.usecase;

import com.wanted.codebombalms.chatbot.application.command.SaveFeedbackCommand;
import com.wanted.codebombalms.chatbot.domain.model.MessageFeedback;

public interface MessageFeedbackCommandUseCase {

    // AI 메시지 평가 설정/전환 (upsert, 멱등)
    MessageFeedback save(SaveFeedbackCommand command);

    // 평가 취소 (없으면 no-op)
    void delete(Long messageId, Long userId);
}
