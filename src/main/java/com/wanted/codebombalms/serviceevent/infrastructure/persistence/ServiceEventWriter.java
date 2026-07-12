package com.wanted.codebombalms.serviceevent.infrastructure.persistence;

import com.wanted.codebombalms.serviceevent.application.port.ServiceEventStore;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * service_event 비동기 적재기 — 적재 실패가 호출측 응답/트랜잭션에 영향 없음(best-effort).
 * 별도 스레드(@Async) 실행 — 예외 삼킴, 호출부 전파 없음.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceEventWriter {

    private final ServiceEventStore store;

    @Async("serviceEventTaskExecutor")
    public void write(ServiceEventEnvelope envelope) {
        try {
            store.save(envelope);
        } catch (Exception e) {
            log.warn("event=service_event_write_failed category={} type={} reason={}",
                    envelope.category().code(), envelope.type().code(), e.toString());
        }
    }
}
