package com.wanted.codebombalms.serviceevent.infrastructure.persistence;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataServiceEventRepository extends JpaRepository<ServiceEventJpaEntity, Long> {

    /**
     * 보존기간 초과분 청크 삭제. 단발 대량 DELETE 금지(롱 락·undo 폭증)라
     * LIMIT 1000 고정 청크로 지운다 — 기존 UserHardDeleteAdapter의 MAX_BATCH 관례.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            DELETE FROM service_event
            WHERE created_at < :threshold
            LIMIT 1000
            """, nativeQuery = true)
    int deleteChunkByCreatedAtBefore(@Param("threshold") LocalDateTime threshold);
}
