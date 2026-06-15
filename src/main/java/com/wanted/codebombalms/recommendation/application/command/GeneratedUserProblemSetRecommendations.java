package com.wanted.codebombalms.recommendation.application.command;

import java.util.List;

/** Python 추천 서버가 반환한 한 사용자 기준의 문제 세트 추천 묶음입니다. */
public record GeneratedUserProblemSetRecommendations(
        Long userId,
        List<GeneratedProblemSetRecommendation> problemSets
) {
}
