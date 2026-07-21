package com.wanted.codebombalms.serviceevent.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataServiceEventRepository extends JpaRepository<ServiceEventJpaEntity, Long> {

    /** 보안 카테고리 목록 — ServiceEventCategory.isSecurity() 와 동기 유지 (native @Query 는 컴파일타임 상수만 허용) */
    String SECURITY_CATEGORIES = "'authn_attack','takeover','oauth','token','signup'";

    /** 보존기간 초과분 1000행 단위 삭제 — 사용처: ServiceEventCleanupConfig(03시 잡) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            DELETE FROM service_event
            WHERE created_at < :threshold
            LIMIT 1000
            """, nativeQuery = true)
    int deleteChunkByCreatedAtBefore(@Param("threshold") LocalDateTime threshold);

    // ===== 이하 summary 집계 — 전부 읽기 전용, LIMIT 은 고정 상수(드라이버 호환) =====

    interface CategoryCountRow { String getCategory(); long getCnt(); }
    interface TypeCountRow { String getEventType(); long getCnt(); }
    interface IpCountRow { String getIpAddress(); long getCnt(); }
    interface RouteAnomalyRow { String getUri(); String getEventType(); long getCnt(); Integer getMaxDuration(); }
    interface HttpStatusBreakdownRow { String getUri(); String getEventType(); Integer getStatus(); long getCnt(); }
    interface HourlyRow { int getHr(); long getCnt(); }
    interface ConcurrentPeakRow { long getPeak(); LocalDateTime getOccurredAt(); }

    @Query(value = """
            SELECT category AS category, COUNT(*) AS cnt
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
            GROUP BY category
            """, nativeQuery = true)
    List<CategoryCountRow> countByCategory(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT event_type AS eventType, COUNT(*) AS cnt
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
            GROUP BY event_type
            """, nativeQuery = true)
    List<TypeCountRow> countByType(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT ip_address AS ipAddress, COUNT(*) AS cnt
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
              AND ip_address IS NOT NULL
              AND category IN (""" + SECURITY_CATEGORIES + """
            )
            GROUP BY ip_address
            ORDER BY cnt DESC
            LIMIT 10
            """, nativeQuery = true)
    List<IpCountRow> findTopSecurityIps(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT event_type
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
              AND ip_address = :ip
              AND category IN (""" + SECURITY_CATEGORIES + """
            )
            GROUP BY event_type
            ORDER BY COUNT(*) DESC
            LIMIT 1
            """, nativeQuery = true)
    String findMainSecurityTypeByIp(
            @Param("ip") String ip, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT DISTINCT user_id
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
              AND ip_address = :ip
              AND user_id IS NOT NULL
              AND category IN (""" + SECURITY_CATEGORIES + """
            )
            LIMIT 5
            """, nativeQuery = true)
    List<Long> findTargetUserIdsByIp(
            @Param("ip") String ip, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT uri AS uri, event_type AS eventType, COUNT(*) AS cnt, MAX(duration_ms) AS maxDuration
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
              AND category = 'http_anomaly'
            GROUP BY uri, event_type
            ORDER BY cnt DESC
            LIMIT 10
            """, nativeQuery = true)
    List<RouteAnomalyRow> findHttpAnomalies(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT uri AS uri, event_type AS eventType, http_status AS status, COUNT(*) AS cnt
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
              AND category = 'http_anomaly'
              AND http_status IS NOT NULL
              AND uri IN (:uris)
            GROUP BY uri, event_type, http_status
            """, nativeQuery = true)
    List<HttpStatusBreakdownRow> findHttpStatusBreakdown(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);

    @Query(value = """
            SELECT HOUR(created_at) AS hr, COUNT(*) AS cnt
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
            GROUP BY HOUR(created_at)
            ORDER BY hr
            """, nativeQuery = true)
    List<HourlyRow> hourlyDistribution(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT target_id AS peak, created_at AS occurredAt
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
              AND event_type = 'concurrent_sample'
              AND target_id IS NOT NULL
            ORDER BY target_id DESC, created_at DESC
            LIMIT 1
            """, nativeQuery = true)
    ConcurrentPeakRow findConcurrentPeak(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ===== 이하 운영 Q&A 챗봇(opschat) 도구용 — 전부 읽기 전용, 필터는 null=전체 =====
    // LIMIT 고정 상수 규칙 유지: 동적 limit 은 OpsQueryAdapter 에서 subList 로 자른다.

    interface TimelineBucketRow { String getBucket(); long getCnt(); }
    interface RecentEventRow {
        String getCategory(); String getEventType(); Long getUserId(); String getIpAddress();
        String getUri(); Integer getHttpStatus(); Integer getDurationMs(); String getTraceId();
        LocalDateTime getCreatedAt();
    }

    @Query(value = """
            SELECT event_type AS eventType, COUNT(*) AS cnt
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
              AND (:category IS NULL OR category = :category)
              AND (:eventType IS NULL OR event_type = :eventType)
            GROUP BY event_type
            ORDER BY cnt DESC
            """, nativeQuery = true)
    List<TypeCountRow> countByTypeFiltered(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            @Param("category") String category, @Param("eventType") String eventType);

    /** 시간(hour) 버킷 추이 — 최대 7일치(168버킷) */
    @Query(value = """
            SELECT DATE_FORMAT(created_at, '%Y-%m-%d %H:00') AS bucket, COUNT(*) AS cnt
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
              AND (:category IS NULL OR category = :category)
              AND (:eventType IS NULL OR event_type = :eventType)
            GROUP BY bucket
            ORDER BY bucket
            LIMIT 168
            """, nativeQuery = true)
    List<TimelineBucketRow> eventTimeline(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            @Param("category") String category, @Param("eventType") String eventType);

    @Query(value = """
            SELECT ip_address AS ipAddress, COUNT(*) AS cnt
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
              AND ip_address IS NOT NULL
              AND (:category IS NULL OR category = :category)
              AND (:eventType IS NULL OR event_type = :eventType)
            GROUP BY ip_address
            ORDER BY cnt DESC
            LIMIT 20
            """, nativeQuery = true)
    List<IpCountRow> topIpsFiltered(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            @Param("category") String category, @Param("eventType") String eventType);

    /** detail 컬럼 미포함(개인정보 방침) — opschat recent_events 도구 전용 */
    @Query(value = """
            SELECT category AS category, event_type AS eventType, user_id AS userId,
                   ip_address AS ipAddress, uri AS uri, http_status AS httpStatus,
                   duration_ms AS durationMs, trace_id AS traceId, created_at AS createdAt
            FROM service_event
            WHERE created_at >= :start AND created_at < :end
              AND (:category IS NULL OR category = :category)
              AND (:eventType IS NULL OR event_type = :eventType)
            ORDER BY created_at DESC
            LIMIT 20
            """, nativeQuery = true)
    List<RecentEventRow> recentEvents(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            @Param("category") String category, @Param("eventType") String eventType);
}
