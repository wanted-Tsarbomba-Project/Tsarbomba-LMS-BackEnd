package com.wanted.codebombalms.recommendation.infrastructure.scheduler;

import com.wanted.codebombalms.recommendation.application.usecase.GenerateProblemSetRecommendationsUseCase;
import java.lang.reflect.Method;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 문제 세트 추천 스케줄러의 실행 위임과 스케줄 설정을 검증합니다. */
class ProblemRecommendationSchedulerTest {

    /** 스케줄러 실행 시 추천 생성 유스케이스를 호출합니다. */
    @Test
    void generateDailyProblemSetRecommendations_callsGenerateUseCase() {
        GenerateProblemSetRecommendationsUseCase useCase = mock(GenerateProblemSetRecommendationsUseCase.class);
        RecommendationBatchLockExecutor lockExecutor = mock(RecommendationBatchLockExecutor.class);
        when(lockExecutor.executeIfLockAcquired(eq("problemRecommendationGeneration"), any()))
                .thenAnswer(invocation -> OptionalInt.of(invocation.getArgument(1, java.util.function.IntSupplier.class).getAsInt()));
        when(useCase.generate()).thenReturn(2);
        ProblemRecommendationScheduler scheduler = new ProblemRecommendationScheduler(useCase, lockExecutor);

        scheduler.generateDailyProblemSetRecommendations();

        verify(useCase).generate();
        verify(lockExecutor).executeIfLockAcquired(eq("problemRecommendationGeneration"), any());
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
        assertEquals("recommendationTaskExecutor", async.value());
        assertEquals("0 0 5 * * *", scheduled.cron());
        assertEquals("Asia/Seoul", scheduled.zone());
    }

    /** 추천 생성 중 예외가 발생해도 스케줄러 메서드는 예외를 외부로 전파하지 않습니다. */
    @Test
    void generateDailyProblemSetRecommendations_whenUseCaseThrows_doesNotPropagateException() {
        GenerateProblemSetRecommendationsUseCase useCase = mock(GenerateProblemSetRecommendationsUseCase.class);
        RecommendationBatchLockExecutor lockExecutor = mock(RecommendationBatchLockExecutor.class);
        when(lockExecutor.executeIfLockAcquired(eq("problemRecommendationGeneration"), any()))
                .thenAnswer(invocation -> OptionalInt.of(invocation.getArgument(1, java.util.function.IntSupplier.class).getAsInt()));
        when(useCase.generate()).thenThrow(new RuntimeException("Python server error"));
        ProblemRecommendationScheduler scheduler = new ProblemRecommendationScheduler(useCase, lockExecutor);

        assertDoesNotThrow(scheduler::generateDailyProblemSetRecommendations);
        verify(useCase).generate();
    }

    /** 다른 서버가 이미 락을 잡고 있으면 추천 생성 유스케이스를 실행하지 않습니다. */
    @Test
    void generateDailyProblemSetRecommendations_whenLockNotAcquired_skipsGenerateUseCase() {
        GenerateProblemSetRecommendationsUseCase useCase = mock(GenerateProblemSetRecommendationsUseCase.class);
        RecommendationBatchLockExecutor lockExecutor = mock(RecommendationBatchLockExecutor.class);
        when(lockExecutor.executeIfLockAcquired(eq("problemRecommendationGeneration"), any()))
                .thenReturn(OptionalInt.empty());
        ProblemRecommendationScheduler scheduler = new ProblemRecommendationScheduler(useCase, lockExecutor);

        scheduler.generateDailyProblemSetRecommendations();

        verify(lockExecutor).executeIfLockAcquired(eq("problemRecommendationGeneration"), any());
        verify(useCase, org.mockito.Mockito.never()).generate();
    }
}
