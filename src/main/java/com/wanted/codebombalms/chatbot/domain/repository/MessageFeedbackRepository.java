package com.wanted.codebombalms.chatbot.domain.repository;

import com.wanted.codebombalms.chatbot.domain.model.MessageFeedback;

import java.util.List;
import java.util.Optional;

public interface MessageFeedbackRepository {

    // 평가 저장 (생성/수정)
    MessageFeedback save(MessageFeedback feedback);

    // messageId 로 평가 단건 조회
    Optional<MessageFeedback> findByMessageId(Long messageId);

    // messageId 묶음으로 평가 일괄 조회 (내역조회 N+1 방지)
    List<MessageFeedback> findByMessageIdIn(List<Long> messageIds);

    // messageId 로 평가 삭제 (없으면 no-op)
    void deleteByMessageId(Long messageId);
}
