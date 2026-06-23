package com.wanted.codebombalms.recommendation.application.usecase;

/** 스케줄러가 문제 세트 추천 생성 배치를 실행할 때 사용하는 유스케이스입니다. */
public interface GenerateProblemSetRecommendationsUseCase {

    /** Python 추천 서버를 호출하고 반환된 추천 결과를 저장합니다. */
    int generate();
}
