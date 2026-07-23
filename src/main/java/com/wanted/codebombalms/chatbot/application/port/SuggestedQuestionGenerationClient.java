package com.wanted.codebombalms.chatbot.application.port;

import com.wanted.codebombalms.chatbot.application.model.GeneratedProblemQuestions;
import com.wanted.codebombalms.chatbot.application.model.ProblemQuestionsGroup;

import java.util.List;

/** FastAPI 추천 질문 생성 서버 호출 포트. */
public interface SuggestedQuestionGenerationClient {

    /** 문제별 원시 질문을 보내 문제별 대표 질문(최대 topN)을 받는다. */
    List<GeneratedProblemQuestions> generate(List<ProblemQuestionsGroup> problems, int topN);
}
