package com.wanted.codebombalms.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataTrustedDeviceRepository extends JpaRepository<TrustedDeviceJpaEntity, Long> {

    Optional<TrustedDeviceJpaEntity> findByUserIdAndDeviceFp(Long userId, String deviceFp);

    List<TrustedDeviceJpaEntity> findAllByUserId(Long userId);

    Optional<TrustedDeviceJpaEntity> findByTrustedDeviceIdAndUserId(Long trustedDeviceId, Long userId);
}
