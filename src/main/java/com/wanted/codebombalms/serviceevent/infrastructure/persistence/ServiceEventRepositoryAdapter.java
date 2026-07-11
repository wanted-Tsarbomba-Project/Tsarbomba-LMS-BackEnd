package com.wanted.codebombalms.serviceevent.infrastructure.persistence;

import com.wanted.codebombalms.serviceevent.application.port.ServiceEventStore;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ServiceEventRepositoryAdapter implements ServiceEventStore {

    private final SpringDataServiceEventRepository repository;

    @Override
    public void save(ServiceEventEnvelope envelope) {
        repository.save(ServiceEventJpaEntity.from(envelope));
    }

    @Override
    @Transactional
    public int deleteChunkCreatedBefore(LocalDateTime threshold) {
        return repository.deleteChunkByCreatedAtBefore(threshold);
    }
}
