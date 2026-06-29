package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.auth.domain.model.TrustedDevice;
import com.wanted.codebombalms.auth.domain.repository.TrustedDeviceRepository;
import com.wanted.codebombalms.user.application.usecase.GetTrustedDevicesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTrustedDevicesService implements GetTrustedDevicesUseCase {

    private final TrustedDeviceRepository trustedDeviceRepository;

    @Override
    public List<TrustedDevice> getTrustedDevices(Long userId) {
        return trustedDeviceRepository.findAllByUserId(userId);
    }
}
