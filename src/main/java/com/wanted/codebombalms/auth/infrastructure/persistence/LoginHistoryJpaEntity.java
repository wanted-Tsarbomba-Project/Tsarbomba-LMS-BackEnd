package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.model.LoginHistory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "login_history",
        indexes = {
                @Index(name = "idx_login_history_user_created_id",
                        columnList = "user_id, created_at, login_history_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class LoginHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_history_id")
    private Long loginHistoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "device_fp", length = 64)
    private String deviceFp;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "is_suspicious", nullable = false)
    private boolean suspicious;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    public static LoginHistoryJpaEntity from(LoginHistory loginHistory) {
        LoginHistoryJpaEntity e = new LoginHistoryJpaEntity();
        e.loginHistoryId = loginHistory.getLoginHistoryId();
        e.userId         = loginHistory.getUserId();
        e.ipAddress      = loginHistory.getIpAddress();
        e.userAgent      = loginHistory.getUserAgent();
        e.deviceFp       = loginHistory.getDeviceFp();
        e.country        = loginHistory.getCountry();
        e.city           = loginHistory.getCity();
        e.suspicious     = loginHistory.isSuspicious();
        return e;
    }


    public LoginHistory toDomain() {
        return LoginHistory.restore(
                loginHistoryId,
                userId,
                ipAddress,
                userAgent,
                deviceFp,
                country,
                city,
                suspicious,
                createdAt
        );
    }
}
