package com.wanted.codebombalms.chatbot.presentation.api.response;

import com.wanted.codebombalms.chatbot.application.model.SuggestedQuestionsResult;

import java.util.List;

/** 추천 질문 서빙 응답. source 로 GENERATED/DEFAULT 구분. */
public record SuggestedQuestionsResponse(
        Long problemSetId,
        Long problemId,
        String source,
        List<String> questions
) {

    public static SuggestedQuestionsResponse of(Long problemSetId, Long problemId, SuggestedQuestionsResult result) {
        return new SuggestedQuestionsResponse(problemSetId, problemId, result.source(), result.questions());
    }
}
