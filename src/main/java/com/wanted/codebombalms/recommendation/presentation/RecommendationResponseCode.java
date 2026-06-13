package com.wanted.codebombalms.recommendation.presentation;

/** recommendation API 성공 응답 코드를 모아둡니다. */
public class RecommendationResponseCode {

    /** 유틸리티 클래스 인스턴스 생성을 막습니다. */
    private RecommendationResponseCode() {
    }

    public static final String PROBLEM_SET_RECOMMENDATIONS_RETRIEVED =
            "REC-PROBLEM-SET-RETRIEVED";
    public static final String PROBLEM_SET_RECOMMENDATIONS_HIDDEN =
            "REC-PROBLEM-SET-HIDDEN";
}
