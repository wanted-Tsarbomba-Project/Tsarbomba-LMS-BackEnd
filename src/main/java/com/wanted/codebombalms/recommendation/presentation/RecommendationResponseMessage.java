package com.wanted.codebombalms.recommendation.presentation;

/** recommendation API 성공 응답 메시지를 모아둡니다. */
public class RecommendationResponseMessage {

    /** 유틸리티 클래스 인스턴스 생성을 막습니다. */
    private RecommendationResponseMessage() {
    }

    public static final String PROBLEM_SET_RECOMMENDATIONS_RETRIEVED =
            "문제 세트 추천 목록 조회 성공";
    public static final String PROBLEM_SET_RECOMMENDATIONS_HIDDEN =
            "문제 세트 추천 오늘 하루 숨김 처리 성공";
}
