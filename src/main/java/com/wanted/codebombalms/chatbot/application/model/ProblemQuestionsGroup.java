package com.wanted.codebombalms.chatbot.application.model;

import java.util.List;

/** 한 문제에 대해 수집한 원시 USER 질문 묶음 (생성 배치 입력). */
public record ProblemQuestionsGroup(
        Long problemSetId,
        Long problemId,
        List<String> questions
) {
}
