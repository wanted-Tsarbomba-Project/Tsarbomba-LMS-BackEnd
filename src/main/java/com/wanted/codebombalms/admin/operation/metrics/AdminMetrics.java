package com.wanted.codebombalms.admin.operation.metrics;

import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/** Admin 운영 도메인 커스텀 메트릭을 기록합니다. */
@Component
public class AdminMetrics {

    private final MeterRegistry registry;
    private final Timer alertListQueryTimer;
    private final Timer alertDetailQueryTimer;
    private final Timer ruleRunTimer;
    private final Timer alertUpsertTimer;

    // admin 도메인에서 사용할 Micrometer Time/ Counter 등록 준비
    public AdminMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.alertListQueryTimer = Timer.builder("admin_operation_alert_list_query_duration")
                .description("운영 알림 목록 조회 DB 구간 시간")
                .register(registry);
        this.alertDetailQueryTimer = Timer.builder("admin_operation_alert_detail_query_duration")
                .description("운영 알림 상세 기본 정보 조회 시간")
                .register(registry);
        this.ruleRunTimer = Timer.builder("admin_operation_rule_run_duration")
                .description("활성화된 운영 자동화 규칙 전체 실행 시간")
                .register(registry);
        this.alertUpsertTimer = Timer.builder("admin_operation_alert_upsert_duration")
                .description("탐지 결과 1건의 open alert 조회와 저장 시간")
                .register(registry);
    }

    // 운영 알림 목록 조회 시간이 얼마나 걸렸는지 기록
    public void recordAlertListQuery(long elapsedNanos) {
        alertListQueryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    // 운영 알림 상세 기본 정보 조회 시간을 기록
    public void recordAlertDetailQuery(long elapsedNanos) {
        alertDetailQueryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    // 알림 대상 상세 조회 시간을 COURSE, PROBLEM, USER 타입별로 기록
    public void recordTargetDetail(OperationTargetType targetType, long elapsedNanos) {
        registry.timer(
                        "admin_operation_alert_target_detail_duration",
                        "targetType",
                        targetType.name()
                )
                .record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    // 자동화 규칙 전체 실행 시간을 기록
    public void recordRuleRun(long elapsedNanos) {
        ruleRunTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    // 특정 규칙 handler 실행 시간을 ruleCode 별로 기록
    public void recordRuleDetect(OperationRuleCode ruleCode, long elapsedNanos) {
        registry.timer(
                        "admin_operation_rule_detect_duration",
                        "ruleCode",
                        ruleCode.name()
                )
                .record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    // 탐지 결과 1건을 alert로 저장/갱신하는 시간을 기록
    public void recordAlertUpsert(long elapsedNanos) {
        alertUpsertTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }


    // 규칙별 탐지 결과 개수를 누적 Counter 로 기록
    public void incrementRuleDetected(OperationRuleCode ruleCode, int detectedCount) {
        if (detectedCount <= 0) {
            return;
        }

        Counter.builder("admin_operation_rule_detected_total")
                .description("규칙별 탐지 결과 누적 수")
                .tag("ruleCode", ruleCode.name())
                .register(registry)
                .increment(detectedCount);
    }
}
