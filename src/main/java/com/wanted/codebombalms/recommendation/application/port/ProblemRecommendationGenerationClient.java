package com.wanted.codebombalms.recommendation.application.port;

import com.wanted.codebombalms.recommendation.application.command.GeneratedUserProblemSetRecommendations;
import java.util.List;

/** 외부 추천 생성 서버 호출을 추상화합니다. */
public interface ProblemRecommendationGenerationClient {

    /** Python 추천 서버에서 사용자별 문제 세트 추천 결과를 생성해 가져옵니다. */
    List<GeneratedUserProblemSetRecommendations> generateProblemSetRecommendations();
}
