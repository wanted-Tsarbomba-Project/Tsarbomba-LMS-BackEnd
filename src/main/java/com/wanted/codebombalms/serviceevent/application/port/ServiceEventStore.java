package com.wanted.codebombalms.serviceevent.application.port;

import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import java.time.LocalDateTime;

/**
 * service_event 영속화 포트.
 * 구현은 infrastructure(JPA)에 두고, 적재기/파기 로직은 이 포트에만 의존한다.
 */
public interface ServiceEventStore {

    void save(ServiceEventEnvelope envelope);

    /**
     * threshold 이전 이벤트를 청크 단위로 삭제하고 삭제 건수를 반환한다.
     * 호출측(파기 배치)이 0이 나올 때까지 반복 호출한다.
     */
    int deleteChunkCreatedBefore(LocalDateTime threshold);
}
