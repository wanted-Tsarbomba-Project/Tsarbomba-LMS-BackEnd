package com.wanted.codebombalms.chatbot.application.model;

import java.util.List;

/** 서빙 결과. source = "GENERATED"(배치 생성) | "DEFAULT"(폴백 기본칩). */
public record SuggestedQuestionsResult(
        String source,
        List<String> questions
) {

    public static SuggestedQuestionsResult generated(List<String> questions) {
        return new SuggestedQuestionsResult("GENERATED", questions);
    }

    public static SuggestedQuestionsResult defaults(List<String> questions) {
        return new SuggestedQuestionsResult("DEFAULT", questions);
    }
}
