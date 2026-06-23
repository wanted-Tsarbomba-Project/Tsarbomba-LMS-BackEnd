package com.wanted.codebombalms.reward.point.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataUserPointRepository extends JpaRepository<UserPointJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select up from UserPointJpaEntity up where up.userId = :userId")
    Optional<UserPointJpaEntity> findByUserIdForUpdate(@Param("userId") Long userId);
    Optional<UserPointJpaEntity> findByUserId(Long userId);
}
