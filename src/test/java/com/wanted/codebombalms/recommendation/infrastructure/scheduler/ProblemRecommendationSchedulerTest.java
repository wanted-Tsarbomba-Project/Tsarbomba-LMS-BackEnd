package com.wanted.codebombalms.recommendation.infrastructure.scheduler;

import com.wanted.codebombalms.recommendation.application.usecase.GenerateProblemSetRecommendationsUseCase;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 문제 세트 추천 스케줄러의 실행 위임과 스케줄 설정을 검증합니다. */
class ProblemRecommendationSchedulerTest {

    /** 스케줄러 실행 시 추천 생성 유스케이스를 호출합니다. */
    @Test
    void generateDailyProblemSetRecommendations_callsGenerateUseCase() {
        GenerateProblemSetRecommendationsUseCase useCase = mock(GenerateProblemSetRecommendationsUseCase.class);
        when(useCase.generate()).thenReturn(2);
        ProblemRecommendationScheduler scheduler = new ProblemRecommendationScheduler(useCase);

        scheduler.generateDailyProblemSetRecommendations();

        verify(useCase).generate();
    }

    /** 매일 새벽 5시 Asia/Seoul 기준으로 비동기 실행되도록 설정되어 있습니다. */
    @Test
    void generateDailyProblemSetRecommendations_hasScheduledAndAsyncAnnotations() throws NoSuchMethodException {
        Method method = ProblemRecommendationScheduler.class
                .getMethod("generateDailyProblemSetRecommendations");

        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        Async async = method.getAnnotation(Async.class);

        assertNotNull(scheduled);
        assertNotNull(async);
        assertEquals("0 0 5 * * *", scheduled.cron());
        assertEquals("Asia/Seoul", scheduled.zone());
    }
}
