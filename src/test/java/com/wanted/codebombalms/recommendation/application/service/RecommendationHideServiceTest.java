package com.wanted.codebombalms.recommendation.application.service;

import com.wanted.codebombalms.recommendation.application.port.RecommendationHidePort;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationHide;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationHideType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/** 추천 오늘 하루 숨김 서비스의 만료 시각 계산을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class RecommendationHideServiceTest {

    @Mock
    private RecommendationHidePort recommendationHidePort;

    @InjectMocks
    private RecommendationHideService service;

    /** Asia/Seoul 기준 오늘 23:59:59를 숨김 만료 시각으로 저장합니다. */
    @Test
    void hideToday_savesUntilTodayEndInSeoul() {
        Long userId = 1L;
        LocalDateTime expectedHiddenUntil = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))
                .atTime(LocalTime.of(23, 59, 59));

        given(recommendationHidePort.saveOrUpdate(
                eq(userId),
                eq(RecommendationHideType.PROBLEM_SET_RECOMMENDATION),
                eq(expectedHiddenUntil)
        )).willReturn(hide(userId, expectedHiddenUntil));

        var result = service.hideToday(userId);

        assertTrue(result.hidden());
        assertEquals(expectedHiddenUntil, result.hiddenUntil());
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
}
