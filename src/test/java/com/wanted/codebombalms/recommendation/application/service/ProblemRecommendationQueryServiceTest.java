package com.wanted.codebombalms.recommendation.application.service;

import com.wanted.codebombalms.recommendation.application.port.ProblemRecommendationQueryPort;
import com.wanted.codebombalms.recommendation.application.port.RecommendationHidePort;
import com.wanted.codebombalms.recommendation.domain.model.ProblemSetRecommendation;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationAlgorithm;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationHide;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationHideType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** 문제 세트 추천 조회 서비스의 숨김/limit 정책을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class ProblemRecommendationQueryServiceTest {

    @Mock
    private ProblemRecommendationQueryPort problemRecommendationQueryPort;

    @Mock
    private RecommendationHidePort recommendationHidePort;

    @InjectMocks
    private ProblemRecommendationQueryService service;

    /** 숨김이 없으면 limit을 최대 3으로 보정해 활성 추천 목록을 조회합니다. */
    @Test
    void getRecommendations_whenNotHidden_returnsActiveRecommendationsWithMaxLimitThree() {
        Long userId = 1L;
        var recommendations = List.of(recommendation(101L, 3001L, 1));

        given(recommendationHidePort.findHide(userId, RecommendationHideType.PROBLEM_SET_RECOMMENDATION))
                .willReturn(Optional.empty());
        given(problemRecommendationQueryPort.findActiveProblemSetRecommendations(userId, 3))
                .willReturn(recommendations);

        var result = service.getRecommendations(userId, 10);

        assertFalse(result.hidden());
        assertEquals(null, result.hiddenUntil());
        assertEquals(recommendations, result.problemSets());
        verify(problemRecommendationQueryPort).findActiveProblemSetRecommendations(userId, 3);
    }

    /** 숨김이 유효하면 추천 목록 저장소를 조회하지 않고 빈 목록을 반환합니다. */
    @Test
    void getRecommendations_whenHiddenUntilFuture_returnsEmptyListWithoutRecommendationQuery() {
        Long userId = 1L;
        var hiddenUntil = LocalDateTime.now().plusDays(1);

        given(recommendationHidePort.findHide(userId, RecommendationHideType.PROBLEM_SET_RECOMMENDATION))
                .willReturn(Optional.of(hide(userId, hiddenUntil)));

        var result = service.getRecommendations(userId, 3);

        assertTrue(result.hidden());
        assertEquals(hiddenUntil, result.hiddenUntil());
        assertTrue(result.problemSets().isEmpty());
        verify(problemRecommendationQueryPort, never()).findActiveProblemSetRecommendations(userId, 3);
    }

    /** 숨김이 만료되었으면 일반 추천 목록 조회 흐름을 수행합니다. */
    @Test
    void getRecommendations_whenHideExpired_queriesRecommendations() {
        Long userId = 1L;
        var hiddenUntil = LocalDateTime.now().minusDays(1);

        given(recommendationHidePort.findHide(userId, RecommendationHideType.PROBLEM_SET_RECOMMENDATION))
                .willReturn(Optional.of(hide(userId, hiddenUntil)));
        given(problemRecommendationQueryPort.findActiveProblemSetRecommendations(userId, 2))
                .willReturn(List.of());

        var result = service.getRecommendations(userId, 2);

        assertFalse(result.hidden());
        assertEquals(null, result.hiddenUntil());
        assertTrue(result.problemSets().isEmpty());
        verify(problemRecommendationQueryPort).findActiveProblemSetRecommendations(userId, 2);
    }

    /** 테스트용 추천 숨김 도메인 모델을 생성합니다. */
    private RecommendationHide hide(Long userId, LocalDateTime hiddenUntil) {
        return new RecommendationHide(
                1L,
                userId,
                RecommendationHideType.PROBLEM_SET_RECOMMENDATION,
                null,
                hiddenUntil,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    /** 테스트용 문제 세트 추천 도메인 모델을 생성합니다. */
    private ProblemSetRecommendation recommendation(Long recommendationId, Long problemSetId, int rankNo) {
        return new ProblemSetRecommendation(
                recommendationId,
                problemSetId,
                10L,
                7L,
                BigDecimal.valueOf(0.034),
                BigDecimal.valueOf(0.72),
                BigDecimal.valueOf(1.85),
                rankNo,
                RecommendationAlgorithm.APRIORI,
                LocalDateTime.now()
        );
    }
}
