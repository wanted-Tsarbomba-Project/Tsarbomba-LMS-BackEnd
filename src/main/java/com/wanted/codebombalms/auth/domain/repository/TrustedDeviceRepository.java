package com.wanted.codebombalms.auth.domain.repository;

import com.wanted.codebombalms.auth.domain.model.TrustedDevice;

import java.util.List;
import java.util.Optional;

public interface TrustedDeviceRepository {

    TrustedDevice save(TrustedDevice device);

    /** 로그인 판정 — 이 기기가 신뢰 목록에 있나 */
    Optional<TrustedDevice> findByUserIdAndDeviceFp(Long userId, String deviceFp);

    /** 마이페이지 — 신뢰 기기 목록 */
    List<TrustedDevice> findAllByUserId(Long userId);

    /** 신뢰 해제 — 소유권 검증용 (없으면 USR-012) */
    Optional<TrustedDevice> findByTrustedDeviceIdAndUserId(Long trustedDeviceId, Long userId);

    void deleteById(Long trustedDeviceId);
}
