package com.wanted.codebombalms.badge.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class BadgeMetrics {

    private final Timer syncTimer;
    private final Timer grantableLookupTimer;
    private final Timer earnedLookupTimer;
    private final Timer saveTimer;

    public BadgeMetrics(MeterRegistry registry) {
        this.syncTimer = Timer.builder("badge_sync_duration")
                .description("Badge synchronization total duration")
                .register(registry);
        this.grantableLookupTimer = Timer.builder("badge_sync_grantable_lookup_duration")
                .description("Grantable badge lookup duration during badge sync")
                .register(registry);
        this.earnedLookupTimer = Timer.builder("badge_sync_earned_lookup_duration")
                .description("Already earned badge lookup duration during badge sync")
                .register(registry);
        this.saveTimer = Timer.builder("badge_sync_save_duration")
                .description("Newly earned user badge save duration during badge sync")
                .register(registry);
    }

    public void recordSync(long elapsedNanos) {
        syncTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordGrantableLookup(long elapsedNanos) {
        grantableLookupTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordEarnedLookup(long elapsedNanos) {
        earnedLookupTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordSave(long elapsedNanos) {
        saveTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
}
