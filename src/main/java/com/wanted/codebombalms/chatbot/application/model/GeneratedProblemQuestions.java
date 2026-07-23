package com.wanted.codebombalms.chatbot.application.model;

import java.util.List;

/** FastAPI가 생성해 돌려준 한 문제의 대표 질문 묶음 (생성 배치 출력). */
public record GeneratedProblemQuestions(
        Long problemSetId,
        Long problemId,
        List<String> questions
) {
}
