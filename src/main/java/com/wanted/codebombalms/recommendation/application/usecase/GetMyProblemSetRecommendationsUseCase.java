package com.wanted.codebombalms.recommendation.application.usecase;

import com.wanted.codebombalms.recommendation.application.query.ProblemSetRecommendationResult;

/** 로그인 사용자의 문제 세트 추천 목록 조회 기능을 정의합니다. */
public interface GetMyProblemSetRecommendationsUseCase {

    /** 사용자 ID와 요청 limit 기준으로 추천 목록 조회 결과를 반환합니다. */
    ProblemSetRecommendationResult getRecommendations(Long userId, Integer limit);
}
