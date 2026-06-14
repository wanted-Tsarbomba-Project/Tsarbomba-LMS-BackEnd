package com.wanted.codebombalms.recommendation.application.usecase;

import com.wanted.codebombalms.recommendation.application.command.RecommendationHideResult;

/** 로그인 사용자의 문제 세트 추천 오늘 하루 숨김 기능을 정의합니다. */
public interface HideProblemSetRecommendationsTodayUseCase {

    /** 사용자 ID 기준으로 추천 영역을 오늘 종료 시각까지 숨김 처리합니다. */
    RecommendationHideResult hideToday(Long userId);
}
