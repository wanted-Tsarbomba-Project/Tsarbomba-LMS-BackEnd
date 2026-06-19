package com.wanted.codebombalms.recommendation.application.service;

import com.wanted.codebombalms.recommendation.application.port.ProblemRecommendationQueryPort;
import com.wanted.codebombalms.recommendation.application.port.RecommendationHidePort;
import com.wanted.codebombalms.recommendation.application.query.ProblemSetRecommendationResult;
import com.wanted.codebombalms.recommendation.application.usecase.GetMyProblemSetRecommendationsUseCase;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationHideType;
import com.wanted.codebombalms.recommendation.infrastructure.metrics.RecommendationMetrics;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 문제 세트 추천 목록 조회와 숨김 상태 판정을 처리합니다. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemRecommendationQueryService implements GetMyProblemSetRecommendationsUseCase {

    private static final int DEFAULT_LIMIT = 3;
    private static final int MAX_LIMIT = 3;
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final ProblemRecommendationQueryPort problemRecommendationQueryPort;
    private final RecommendationHidePort recommendationHidePort;
    private final RecommendationMetrics recommendationMetrics;

    /** 숨김 상태를 먼저 확인한 뒤 활성 추천 목록을 최대 3개까지 조회합니다. */
    @Override
    public ProblemSetRecommendationResult getRecommendations(Long userId, Integer limit) {
        long startedAt = System.nanoTime();
        LocalDateTime now = LocalDateTime.now(SEOUL_ZONE);

        ProblemSetRecommendationResult result = recommendationHidePort.findHide(userId, RecommendationHideType.PROBLEM_SET_RECOMMENDATION)
                .filter(hide -> hide.isHiddenAt(now))
                .map(hide -> new ProblemSetRecommendationResult(true, hide.hiddenUntil(), List.of()))
                .orElseGet(() -> new ProblemSetRecommendationResult(
                        false,
                        null,
                        problemRecommendationQueryPort.findActiveProblemSetRecommendations(userId, clampLimit(limit))
                ));
        long elapsedNanos = System.nanoTime() - startedAt;

        recommendationMetrics.recordProblemSetList(elapsedNanos);
        recommendationMetrics.incrementExposed(result.problemSets().size());
        log.info("event=recommendation_problem_set_list_queried hidden={} resultCount={} durationMs={}",
                result.hidden(), result.problemSets().size(), elapsedNanos / 1_000_000);

        return result;
    }

    /** 요청 limit을 기본값 3, 최대값 3 범위로 보정합니다. */
    private int clampLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.max(1, Math.min(limit, MAX_LIMIT));
    }
}
