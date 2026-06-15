package com.wanted.codebombalms.recommendation.application.port;

import com.wanted.codebombalms.recommendation.application.command.GeneratedUserProblemSetRecommendations;

/** 추천 생성 결과 저장에 필요한 쓰기 저장소 접근을 추상화합니다. */
public interface ProblemRecommendationCommandPort {

    /** 사용자의 기존 활성 추천을 비활성화하고 새 추천 결과를 활성 상태로 저장합니다. */
    void replaceActiveRecommendations(GeneratedUserProblemSetRecommendations recommendations);
}
