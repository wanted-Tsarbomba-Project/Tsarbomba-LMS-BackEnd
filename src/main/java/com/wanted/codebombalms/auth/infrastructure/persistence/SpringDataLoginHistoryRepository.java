package com.wanted.codebombalms.auth.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataLoginHistoryRepository extends JpaRepository<LoginHistoryJpaEntity, Long> {

    List<LoginHistoryJpaEntity> findByUserIdOrderByCreatedAtDescLoginHistoryIdDesc(Long userId, Pageable pageable);

    @Query("""
            select lh.userId as userId, max(lh.createdAt) as latestLoginAt
            from LoginHistoryJpaEntity lh
            where lh.userId in :userIds
            group by lh.userId
            """)
    List<LatestLoginAtProjection> findLatestLoginAtByUserIds(@Param("userIds") List<Long> userIds);

    interface LatestLoginAtProjection {
        Long getUserId();

        LocalDateTime getLatestLoginAt();
    }

    /** 기간 내 로그인 고유 회원 수 조회 — 보안 요약 KPI 용 읽기 전용 */
    @Query("""
            select count(distinct lh.userId)
            from LoginHistoryJpaEntity lh
            where lh.createdAt >= :start and lh.createdAt < :end
            """)
    long countDistinctUserIdBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
