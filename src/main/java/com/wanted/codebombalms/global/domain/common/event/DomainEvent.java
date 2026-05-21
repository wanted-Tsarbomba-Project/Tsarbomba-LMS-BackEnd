package com.wanted.codebombalms.global.domain.common.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}