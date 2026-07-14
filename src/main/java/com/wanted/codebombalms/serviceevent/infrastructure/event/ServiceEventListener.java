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
 * 서비스 이벤트 수집 리스너 — 신규 발행 envelope 과 기존 도메인 이벤트 3종을 적재.
 * AFTER_COMMIT — 성공 커밋만 기록. 예외는 모두 흡수 — 같은 이벤트의 다른 리스너에 비전파.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceEventListener {

    private final ServiceEventWriter serviceEventWriter;

    /** 신규 발행 경로 — fallbackExecution=true: 트랜잭션 밖 발행도 수신 */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onServiceEvent(ServiceEventEnvelope envelope) {
        forward(envelope);
    }

    /** 기존 이벤트 구독 — 문제 정답 (submission 발행) */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProblemSolved(ProblemSolvedEvent event) {
        forward(ServiceEventEnvelope.business(
                ServiceEventType.PROBLEM_SOLVED, event.userId(), event.problemId(),
                "submissionId=" + event.submissionId() + " point=" + event.point()));
    }

    /** 기존 이벤트 구독 — 문제집 완료 (submission 발행) */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProblemSetCompleted(ProblemSetCompletedEvent event) {
        forward(ServiceEventEnvelope.business(
                ServiceEventType.PROBLEM_SET_COMPLETED, event.userId(), event.problemSetId(), null));
    }

    /** 기존 이벤트 구독 — 포인트 지급 (reward.point 발행) */
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
