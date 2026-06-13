package com.wanted.codebombalms.recommendation.application.service;

import com.wanted.codebombalms.recommendation.application.command.RecommendationHideResult;
import com.wanted.codebombalms.recommendation.application.port.RecommendationHidePort;
import com.wanted.codebombalms.recommendation.application.usecase.HideProblemSetRecommendationsTodayUseCase;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationHide;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationHideType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 문제 세트 추천을 오늘 하루 숨김 처리합니다. */
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationHideService implements HideProblemSetRecommendationsTodayUseCase {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final RecommendationHidePort recommendationHidePort;

    /** Asia/Seoul 기준 오늘 23:59:59까지 숨김 만료 시간을 저장합니다. */
    @Override
    public RecommendationHideResult hideToday(Long userId) {
        LocalDateTime hiddenUntil = LocalDate.now(SEOUL_ZONE).atTime(LocalTime.of(23, 59, 59));
        RecommendationHide hide = recommendationHidePort.saveOrUpdate(
                userId,
                RecommendationHideType.PROBLEM_SET_RECOMMENDATION,
                hiddenUntil
        );

        return new RecommendationHideResult(true, hide.hiddenUntil());
    }
}
