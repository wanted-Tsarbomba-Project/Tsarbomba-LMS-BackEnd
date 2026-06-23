package com.wanted.codebombalms.user.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * User 도메인 커스텀 메트릭 (관측 Layer 3).
 *
 * <p>HTTP 메트릭({@code http_server_requests})은 요청 전체(컨트롤러~직렬화) 시간만 안다.
 * 이 Timer 는 "학생 목록 조회 내부 구간(list + count 쿼리)"만 분리 측정한다. 부하 중
 * HTTP p95 ↑ 와 이 timer ↑ 가 함께 움직이면 병목 = 목록 쿼리(인덱스 부재 풀스캔/filesort)
 * 로 좁힐 수 있다. 인덱스 반영 후 이 timer 가 떨어지면 최적화 효과를 직접 증명한다.
 */
@Component
public class UserMetrics {

    private final Timer studentListQueryTimer;

    public UserMetrics(MeterRegistry registry) {
        // 등록명엔 _seconds 를 붙이지 않는다. Timer 라서 Prometheus 가
        // user_student_list_query_duration_seconds_{count,sum,max} 로 자동 변환한다.
        this.studentListQueryTimer = Timer.builder("user_student_list_query_duration")
                .description("학생 목록 조회 구간 시간(list 쿼리 + count 쿼리)")
                .register(registry);
    }

    /** 학생 목록 조회(list + count) 구간 소요 시간 기록. */
    public void recordStudentListQuery(long elapsedNanos) {
        studentListQueryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
}
