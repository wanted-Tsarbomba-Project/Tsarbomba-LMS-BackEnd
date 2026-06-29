package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.model.TrustedDevice;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "trusted_devices",
        uniqueConstraints = {
                @UniqueConstraint(name = "idx_trusted_devices_user_fp",
                        columnNames = {"user_id", "device_fp"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TrustedDeviceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trusted_device_id")
    private Long trustedDeviceId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_fp", nullable = false, length = 64)
    private String deviceFp;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "last_country", length = 50)
    private String lastCountry;

    @Column(name = "last_city", length = 50)
    private String lastCity;

    @Column(name = "last_used_at", nullable = false)
    private LocalDateTime lastUsedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    public static TrustedDeviceJpaEntity from(TrustedDevice device) {
        TrustedDeviceJpaEntity e = new TrustedDeviceJpaEntity();
        e.trustedDeviceId = device.getTrustedDeviceId();
        e.userId          = device.getUserId();
        e.deviceFp        = device.getDeviceFp();
        e.deviceName      = device.getDeviceName();
        e.lastCountry     = device.getLastCountry();
        e.lastCity        = device.getLastCity();
        e.lastUsedAt      = device.getLastUsedAt();
        return e;
    }



    public TrustedDevice toDomain() {
        return TrustedDevice.restore(
                trustedDeviceId,
                userId,
                deviceFp,
                deviceName,
                lastCountry,
                lastCity,
                lastUsedAt,
                createdAt
        );
    }
}
