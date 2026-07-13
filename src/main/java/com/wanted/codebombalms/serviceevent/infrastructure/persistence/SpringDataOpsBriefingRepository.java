package com.wanted.codebombalms.serviceevent.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SpringDataOpsBriefingRepository extends JpaRepository<OpsBriefingJpaEntity, Long> {

    Optional<OpsBriefingJpaEntity> findTopByStatusOrderByGeneratedAtDesc(String status);

    Optional<OpsBriefingJpaEntity> findTopByOrderByGeneratedAtDesc();

    /** 보존 2개월 초과분 삭제 — HardDelete 실행 경로 트랜잭션 부재, 여기서 @Transactional 필수 */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            DELETE FROM ops_briefing
            WHERE generated_at < :threshold
            """, nativeQuery = true)
    int deleteByGeneratedAtBefore(@Param("threshold") LocalDateTime threshold);
}
