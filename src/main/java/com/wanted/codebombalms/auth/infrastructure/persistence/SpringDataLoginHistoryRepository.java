package com.wanted.codebombalms.auth.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataLoginHistoryRepository extends JpaRepository<LoginHistoryJpaEntity, Long> {

    List<LoginHistoryJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

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
}
