package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.auth.domain.model.TrustedDevice;
import com.wanted.codebombalms.auth.domain.repository.TrustedDeviceRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.user.application.usecase.RemoveTrustedDeviceUseCase;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RemoveTrustedDeviceService implements RemoveTrustedDeviceUseCase {

    private final TrustedDeviceRepository trustedDeviceRepository;

    @Override
    public void remove(Long userId, Long trustedDeviceId) {
        // 본인 소유 확인 — 없으면 404 (USR-012)
        TrustedDevice device = trustedDeviceRepository
                .findByTrustedDeviceIdAndUserId(trustedDeviceId, userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.TRUSTED_DEVICE_NOT_FOUND));

        trustedDeviceRepository.deleteById(device.getTrustedDeviceId());
    }
}
