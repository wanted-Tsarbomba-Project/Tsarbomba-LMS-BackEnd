package com.wanted.codebombalms.serviceevent.infrastructure.persistence;

import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "service_event")
public class ServiceEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_event_id")
    private Long id;

    @Column(name = "category", nullable = false, length = 30)
    private String category;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "uri", length = 255)
    private String uri;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "trace_id", length = 8)
    private String traceId;

    @Column(name = "detail", length = 500)
    private String detail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ServiceEventJpaEntity from(ServiceEventEnvelope envelope) {
        ServiceEventJpaEntity entity = new ServiceEventJpaEntity();
        entity.category = envelope.category().code();
        entity.eventType = envelope.type().code();
        entity.userId = envelope.userId();
        entity.targetId = envelope.targetId();
        entity.ipAddress = envelope.ipAddress();
        entity.uri = envelope.uri();
        entity.httpStatus = envelope.httpStatus();
        entity.durationMs = envelope.durationMs();
        entity.traceId = envelope.traceId();
        entity.detail = envelope.detail();
        entity.createdAt = envelope.occurredAt();
        return entity;
    }
}
