package com.wanted.codebombalms.chatbot.application.usecase;

/** 문제별 추천 질문 생성 배치. 저장한 문제 수를 반환한다. */
public interface GenerateSuggestedQuestionsUseCase {

    int generate();
}
