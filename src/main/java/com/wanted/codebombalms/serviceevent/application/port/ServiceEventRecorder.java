package com.wanted.codebombalms.serviceevent.application.port;

import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;

/**
 * 비즈니스 이벤트 발행 포트.
 * AFTER_COMMIT 기록 — 롤백된 행위는 미기록.
 */
public interface ServiceEventRecorder {

    void record(ServiceEventEnvelope envelope);
}
