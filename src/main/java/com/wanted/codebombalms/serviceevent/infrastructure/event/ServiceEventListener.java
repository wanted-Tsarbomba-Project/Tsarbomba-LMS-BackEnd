package com.wanted.codebombalms.serviceevent.infrastructure.event;

import com.wanted.codebombalms.reward.point.domain.event.PointGrantedEvent;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventType;
import com.wanted.codebombalms.serviceevent.infrastructure.persistence.ServiceEventWriter;
import com.wanted.codebombalms.submission.domain.event.ProblemSetCompletedEvent;
import com.wanted.codebombalms.submission.domain.event.ProblemSolvedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 갈래 A 수집 리스너
 *
 * ① 신규 발행 경로: 도메인이 ServiceEventRecorder 로 발행한 envelope 을 커밋 후 적재.
 * ② 기존 이벤트 경로: submission·reward 가 이미 발행 중인 이벤트 3종을 구독만 한다
 *    — 발행측 코드 0줄, 기존 리스너(포인트 지급·뱃지 동기화)와 완전 독립.
 *
 * 모든 예외는 여기서 삼킨다 — 같은 이벤트를 구독하는 다른 리스너에 절대 전파하지 않는다.
 * 무거운 작업(INSERT)은 writer 의 @Async 풀에서 실행되므로 이 리스너 자체는
 * 큐 등록만 하고 즉시 반환한다 (발행 스레드 부담 = 마이크로초 단위).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceEventListener {

    private final ServiceEventWriter serviceEventWriter;

    /** 신규 발행 경로 — fallbackExecution: 트랜잭션 밖 발행(리액티브 흐름 등)도 수집 */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onServiceEvent(ServiceEventEnvelope envelope) {
        forward(envelope);
    }

    /** 기존 이벤트 구독 — 문제 정답 (submission 발행, 코드 무변경) */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProblemSolved(ProblemSolvedEvent event) {
        forward(ServiceEventEnvelope.business(
                ServiceEventType.PROBLEM_SOLVED, event.userId(), event.problemId(),
                "submissionId=" + event.submissionId() + " point=" + event.point()));
    }

    /** 기존 이벤트 구독 — 문제집 완료 (submission 발행, 코드 무변경) */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProblemSetCompleted(ProblemSetCompletedEvent event) {
        forward(ServiceEventEnvelope.business(
                ServiceEventType.PROBLEM_SET_COMPLETED, event.userId(), event.problemSetId(), null));
    }

    /** 기존 이벤트 구독 — 포인트 지급 (reward.point 발행, 코드 무변경) */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPointGranted(PointGrantedEvent event) {
        forward(ServiceEventEnvelope.business(
                ServiceEventType.POINT_GRANTED, event.userId(), null,
                "totalPoint=" + event.totalPoint()));
    }

    private void forward(ServiceEventEnvelope envelope) {
        try {
            serviceEventWriter.write(envelope);
        } catch (Exception e) {
            log.warn("event=service_event_listener_failed type={}",
                    envelope == null ? "-" : envelope.type().code(), e);
        }
    }
}
