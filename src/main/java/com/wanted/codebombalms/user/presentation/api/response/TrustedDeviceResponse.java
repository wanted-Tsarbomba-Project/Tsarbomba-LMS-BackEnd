package com.wanted.codebombalms.user.presentation.api.response;

import com.wanted.codebombalms.auth.domain.model.TrustedDevice;

import java.time.LocalDateTime;

public record TrustedDeviceResponse(
        Long id,
        String deviceName,
        String lastCountry,
        String lastCity,
        LocalDateTime lastUsedAt,
        boolean current
) {
    public static TrustedDeviceResponse from(TrustedDevice device, String currentDeviceFp) {
        return new TrustedDeviceResponse(
                device.getTrustedDeviceId(),
                device.getDeviceName(),
                device.getLastCountry(),
                device.getLastCity(),
                device.getLastUsedAt(),
                device.getDeviceFp().equals(currentDeviceFp)
        );
    }
}
