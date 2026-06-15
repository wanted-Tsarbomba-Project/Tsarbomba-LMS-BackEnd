package com.wanted.codebombalms.recommendation.infrastructure.scheduler;

import com.wanted.codebombalms.recommendation.application.usecase.GenerateProblemSetRecommendationsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 매일 새벽 Python 추천 서버를 호출해 문제 세트 추천 결과를 갱신합니다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemRecommendationScheduler {

    private final GenerateProblemSetRecommendationsUseCase generateUseCase;

    /** 매일 새벽 5시(Asia/Seoul)에 비동기로 추천 생성 배치를 실행합니다. */
    @Async
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    public void generateDailyProblemSetRecommendations() {
        try {
            int userCount = generateUseCase.generate();
            log.info("문제 세트 추천 생성 배치를 완료했습니다. userCount={}", userCount);
        } catch (Exception e) {
            log.error("문제 세트 추천 생성 배치 중 예외가 발생했습니다.", e);
        }
    }
}
