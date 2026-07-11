package com.wanted.codebombalms.serviceevent.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataOpsBriefingRepository extends JpaRepository<OpsBriefingJpaEntity, Long> {

    Optional<OpsBriefingJpaEntity> findTopByStatusOrderByGeneratedAtDesc(String status);

    Optional<OpsBriefingJpaEntity> findTopByOrderByGeneratedAtDesc();

    /** 보존 2개월 파기 — 하루 3행(스케줄) + 수동 소량이라 청크 루프 불필요 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            DELETE FROM ops_briefing
            WHERE generated_at < :threshold
            """, nativeQuery = true)
    int deleteByGeneratedAtBefore(@Param("threshold") LocalDateTime threshold);
}
