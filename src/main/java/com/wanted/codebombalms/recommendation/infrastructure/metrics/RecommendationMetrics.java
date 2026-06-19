package com.wanted.codebombalms.recommendation.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/** Recommendation 도메인 커스텀 메트릭을 기록합니다. */
@Component
public class RecommendationMetrics {

    private final MeterRegistry registry;
    private final Timer problemSetListTimer;
    private final Timer hideLookupTimer;
    private final Timer problemSetListQueryTimer;
    private final Timer generationBatchTimer;
    private final Timer generationExternalTimer;
    private final Counter generationUserCounter;
    private final Counter exposedCounter;
    private final DistributionSummary confidenceSummary;
    private final DistributionSummary liftSummary;
    private final DistributionSummary supportSummary;

    // recommendation 도메인의 Time/Counter/DistributionSummary 등록
    public RecommendationMetrics(MeterRegistry registry) {
        this.registry = registry;

        // 추천 목록 조회 전체 시간 기록
        this.problemSetListTimer = Timer.builder("recommendation_problem_set_list_duration")
                .description("추천 목록 조회 전체 시간")
                .register(registry);

        // 추천 숨김 상태 조회 시간 기록
        this.hideLookupTimer = Timer.builder("recommendation_hide_lookup_duration")
                .description("추천 숨김 상태 조회 시간")
                .register(registry);

        // ACTIVE 추천 목록 native query 시간 기록
        this.problemSetListQueryTimer = Timer.builder("recommendation_problem_set_list_query_duration")
                .description("ACTIVE 추천 목록 native query 시간")
                .register(registry);

        // 추천 생성 배치 전체 시간 기록
        this.generationBatchTimer = Timer.builder("recommendation_generation_batch_duration")
                .description("추천 생성 배치 전체 시간")
                .register(registry);

        // Java -> Python FastAPI 호출 왕복 시간 기록
        this.generationExternalTimer = Timer.builder("recommendation_generation_external_duration")
                .description("Java에서 Python FastAPI를 호출하는 왕복 시간")
                .register(registry);

        // 사용자별 추천 저장 시간을 success/failed 상태별로 기록
        this.generationUserCounter = Counter.builder("recommendation_generation_user_total")
                .description("추천이 생성된 사용자 수 누적")
                .register(registry);

        // 추천 생성 대상 사용자 수 누적
        this.exposedCounter = Counter.builder("recommendation_problem_set_exposed_total")
                .description("추천 목록에서 사용자에게 노출된 추천 카드 수")
                .register(registry);

        // 추천 생성 실패 수를 reason 별로 기록
        this.confidenceSummary = DistributionSummary.builder("recommendation_score_confidence")
                .description("생성된 추천 confidence 분포")
                .register(registry);

        // 추천 카드 노출 수 누적
        this.liftSummary = DistributionSummary.builder("recommendation_score_lift")
                .description("생성된 추천 lift 분포")
                .register(registry);
        this.supportSummary = DistributionSummary.builder("recommendation_score_support")
                .description("생성된 추천 support 분포")
                .register(registry);
    }

    public void recordProblemSetList(long elapsedNanos) {
        problemSetListTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordHideLookup(long elapsedNanos) {
        hideLookupTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordProblemSetListQuery(long elapsedNanos) {
        problemSetListQueryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordGenerationBatch(long elapsedNanos) {
        generationBatchTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordGenerationExternal(long elapsedNanos) {
        generationExternalTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordGenerationSave(String status, long elapsedNanos) {
        registry.timer("recommendation_generation_save_duration", "status", status)
                .record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void incrementGenerationUsers(int userCount) {
        if (userCount > 0) {
            generationUserCounter.increment(userCount);
        }
    }

    public void incrementGenerationFailed(String reason) {
        Counter.builder("recommendation_generation_failed_total")
                .description("추천 생성 실패 수")
                .tag("reason", reason)
                .register(registry)
                .increment();
    }

    public void incrementExposed(int exposedCount) {
        if (exposedCount > 0) {
            exposedCounter.increment(exposedCount);
        }
    }

    // 추천 score인 confidence/lift/support 분포 기록
    public void recordScores(BigDecimal confidence, BigDecimal lift, BigDecimal support) {
        recordIfPresent(confidenceSummary, confidence);
        recordIfPresent(liftSummary, lift);
        recordIfPresent(supportSummary, support);
    }

    private void recordIfPresent(DistributionSummary summary, BigDecimal value) {
        if (value != null) {
            summary.record(value.doubleValue());
        }
    }
}
