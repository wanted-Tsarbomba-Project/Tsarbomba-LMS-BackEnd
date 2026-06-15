package com.wanted.codebombalms.recommendation.application.port;

import com.wanted.codebombalms.recommendation.domain.model.ProblemSetRecommendation;
import java.util.List;

/** 추천 목록 조회에 필요한 저장소 접근을 추상화합니다. */
public interface ProblemRecommendationQueryPort {

    /** 사용자의 활성 문제 세트 추천 목록을 제한 개수만큼 조회합니다. */
    List<ProblemSetRecommendation> findActiveProblemSetRecommendations(Long userId, int limit);
}
