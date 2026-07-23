package com.wanted.codebombalms.chatbot.application.usecase;

import com.wanted.codebombalms.chatbot.application.model.SuggestedQuestionsResult;

/** 문제풀이방 진입 시 노출할 추천 질문(칩) 조회. 없으면 DEFAULT 기본칩. */
public interface GetSuggestedQuestionsUseCase {

    SuggestedQuestionsResult get(Long problemSetId, Long problemId);
}
