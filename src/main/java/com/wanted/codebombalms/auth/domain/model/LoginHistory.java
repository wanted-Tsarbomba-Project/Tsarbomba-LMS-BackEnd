package com.wanted.codebombalms.auth.domain.model;

import java.time.LocalDateTime;

public class LoginHistory {

    private Long loginHistoryId;
    private Long userId;
    private String ipAddress;
    private String userAgent;
    private String deviceFp;
    private String country;
    private String city;
    private boolean suspicious;
    private LocalDateTime createdAt;

    private LoginHistory() {}

    // ===== Getter =====
    public Long getLoginHistoryId()    { return loginHistoryId; }
    public Long getUserId()            { return userId; }
    public String getIpAddress()       { return ipAddress; }
    public String getUserAgent()       { return userAgent; }
    public String getDeviceFp()        { return deviceFp; }
    public String getCountry()         { return country; }
    public String getCity()            { return city; }
    public boolean isSuspicious()      { return suspicious; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ===== 정적 팩토리 — 신규 기록 =====

    public static LoginHistory record(
            Long userId,
            String ipAddress,
            String userAgent,
            String deviceFp,
            String country,
            String city,
            boolean suspicious
    ) {
        LoginHistory history = new LoginHistory();
        history.userId      = userId;
        history.ipAddress   = ipAddress;
        history.userAgent   = userAgent;
        history.deviceFp    = deviceFp;
        history.country     = country;
        history.city        = city;
        history.suspicious  = suspicious;
        return history;
    }

    // ===== 영속성 복원 — Adapter 전용 =====

    public static LoginHistory restore(
            Long loginHistoryId,
            Long userId,
            String ipAddress,
            String userAgent,
            String deviceFp,
            String country,
            String city,
            boolean suspicious,
            LocalDateTime createdAt
    ) {
        LoginHistory history = new LoginHistory();
        history.loginHistoryId = loginHistoryId;
        history.userId         = userId;
        history.ipAddress      = ipAddress;
        history.userAgent      = userAgent;
        history.deviceFp       = deviceFp;
        history.country        = country;
        history.city           = city;
        history.suspicious     = suspicious;
        history.createdAt      = createdAt;
        return history;
    }
}
