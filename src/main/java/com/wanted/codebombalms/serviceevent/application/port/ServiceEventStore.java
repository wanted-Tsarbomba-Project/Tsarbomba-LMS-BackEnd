package com.wanted.codebombalms.serviceevent.application.port;

import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import java.time.LocalDateTime;

/** service_event 영속화 포트. */
public interface ServiceEventStore {

    void save(ServiceEventEnvelope envelope);

    /** threshold 이전 이벤트를 청크 단위로 삭제 후 건수 반환 — 호출측이 0이 될 때까지 반복 호출 */
    int deleteChunkCreatedBefore(LocalDateTime threshold);
}
