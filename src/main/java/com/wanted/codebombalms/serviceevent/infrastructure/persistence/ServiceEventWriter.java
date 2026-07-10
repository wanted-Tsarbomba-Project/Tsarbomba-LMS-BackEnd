package com.wanted.codebombalms.serviceevent.infrastructure.persistence;

import com.wanted.codebombalms.serviceevent.application.port.ServiceEventStore;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * service_event 비동기 적재기 — 갈래 B/C의 직호출 대상이자 갈래 A 리스너의 최종 목적지.
 * 원칙: 적재 실패가 호출측 응답/트랜잭션에 절대 영향을 주지 않는다 (best-effort).
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
