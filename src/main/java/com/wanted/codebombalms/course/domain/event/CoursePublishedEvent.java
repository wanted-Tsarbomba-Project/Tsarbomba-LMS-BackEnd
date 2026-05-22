package com.wanted.codebombalms.course.domain.event;

import com.wanted.codebombalms.global.domain.common.event.DomainEvent;

import java.time.Instant;

public record CoursePublishedEvent(
        Long courseId,
        Instant occurredAt
) implements DomainEvent {
}
