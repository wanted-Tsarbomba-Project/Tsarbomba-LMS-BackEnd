package com.wanted.codebombalms.auth.domain.model;

import java.time.LocalDateTime;

public class TrustedDevice {

    private Long trustedDeviceId;
    private Long userId;
    private String deviceFp;
    private String deviceName;
    private String lastCountry;
    private String lastCity;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;

    private TrustedDevice() {}

    // ===== Getter =====
    public Long getTrustedDeviceId() { return trustedDeviceId; }
    public Long getUserId()          { return userId; }
    public String getDeviceFp()      { return deviceFp; }
    public String getDeviceName()    { return deviceName; }
    public String getLastCountry()   { return lastCountry; }
    public String getLastCity()      { return lastCity; }
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    // ===== 정적 팩토리 — 신규 신뢰 등록 =====

    public static TrustedDevice register(
            Long userId,
            String deviceFp,
            String deviceName,
            String lastCountry,
            String lastCity
    ) {
        TrustedDevice device = new TrustedDevice();
        device.userId      = userId;
        device.deviceFp    = deviceFp;
        device.deviceName  = deviceName;
        device.lastCountry = lastCountry;
        device.lastCity    = lastCity;
        device.lastUsedAt  = LocalDateTime.now();
        return device;
    }

    // ===== 신뢰 기기 재사용 시 최신화 =====

    public void markUsed(String lastCountry, String lastCity) {
        this.lastCountry = lastCountry;
        this.lastCity    = lastCity;
        this.lastUsedAt  = LocalDateTime.now();
    }

    // ===== 영속성 복원 — Adapter 전용 =====

    public static TrustedDevice restore(
            Long trustedDeviceId,
            Long userId,
            String deviceFp,
            String deviceName,
            String lastCountry,
            String lastCity,
            LocalDateTime lastUsedAt,
            LocalDateTime createdAt
    ) {
        TrustedDevice device = new TrustedDevice();
        device.trustedDeviceId = trustedDeviceId;
        device.userId          = userId;
        device.deviceFp        = deviceFp;
        device.deviceName      = deviceName;
        device.lastCountry     = lastCountry;
        device.lastCity        = lastCity;
        device.lastUsedAt      = lastUsedAt;
        device.createdAt       = createdAt;
        return device;
    }
}
