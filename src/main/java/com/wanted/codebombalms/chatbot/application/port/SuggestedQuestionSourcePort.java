package com.wanted.codebombalms.chatbot.application.port;

import com.wanted.codebombalms.chatbot.application.model.ProblemQuestionsGroup;

import java.util.List;

/** 추천 질문 생성의 원천(USER 질문) 조회 포트. */
public interface SuggestedQuestionSourcePort {

    /**
     * 임계값(유저수·질문수)을 넘는 문제들의 최신 USER 질문을 문제당 최대 perProblemLimit개 모아 반환.
     */
    List<ProblemQuestionsGroup> findEligibleProblemQuestions(int minUsers, int minQuestions, int perProblemLimit);
}
