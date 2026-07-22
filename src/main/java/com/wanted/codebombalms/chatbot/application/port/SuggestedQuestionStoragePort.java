package com.wanted.codebombalms.chatbot.application.port;

import java.util.List;

/** 추천 질문 저장·조회 포트 (problem_suggested_question). */
public interface SuggestedQuestionStoragePort {

    /** (문제세트, 문제)의 기존 추천 질문을 통째 교체(delete + insert)한다. */
    void replace(Long problemSetId, Long problemId, List<String> questions);

    /** (문제세트, 문제)의 저장된 추천 질문을 rank 순으로 조회. 없으면 빈 목록. */
    List<String> findQuestions(Long problemSetId, Long problemId);
}
