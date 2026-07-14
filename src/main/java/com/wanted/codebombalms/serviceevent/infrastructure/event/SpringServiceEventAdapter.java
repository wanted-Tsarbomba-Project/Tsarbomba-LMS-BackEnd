package com.wanted.codebombalms.serviceevent.infrastructure.event;

import com.wanted.codebombalms.serviceevent.application.port.ServiceEventRecorder;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 발행 어댑터 — envelope 를 Spring ApplicationEvent 로 발행.
 */
@Component
@RequiredArgsConstructor
public class SpringServiceEventAdapter implements ServiceEventRecorder {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void record(ServiceEventEnvelope envelope) {
        eventPublisher.publishEvent(envelope);
    }
}
