package com.wanted.codebombalms.serviceevent.infrastructure.persistence;

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
@Table(name = "ops_briefing")
public class OpsBriefingJpaEntity {

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ops_briefing_id")
    private Long id;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "model", nullable = false, length = 40)
    private String model;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "content_json", columnDefinition = "TEXT")
    private String contentJson;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    public static OpsBriefingJpaEntity success(
            LocalDateTime periodStart, LocalDateTime periodEnd, String model, String contentJson) {
        return of(periodStart, periodEnd, model, STATUS_SUCCESS, contentJson);
    }

    public static OpsBriefingJpaEntity failed(
            LocalDateTime periodStart, LocalDateTime periodEnd, String model) {
        return of(periodStart, periodEnd, model, STATUS_FAILED, null);
    }

    private static OpsBriefingJpaEntity of(
            LocalDateTime periodStart, LocalDateTime periodEnd, String model, String status, String contentJson) {
        OpsBriefingJpaEntity entity = new OpsBriefingJpaEntity();
        entity.periodStart = periodStart;
        entity.periodEnd = periodEnd;
        entity.model = model;
        entity.status = status;
        entity.contentJson = contentJson;
        entity.generatedAt = LocalDateTime.now();
        return entity;
    }

    public boolean isSuccess() {
        return STATUS_SUCCESS.equals(status);
    }
}
