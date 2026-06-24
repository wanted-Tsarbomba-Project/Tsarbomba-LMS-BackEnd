package com.wanted.codebombalms.user.presentation.api.response;

import com.wanted.codebombalms.auth.domain.model.LoginHistory;

import java.time.LocalDateTime;

public record LoginHistoryResponse(
        Long loginHistoryId,
        String ipAddress,
        String country,
        String city,
        boolean isSuspicious,
        LocalDateTime createdAt
) {
    public static LoginHistoryResponse from(LoginHistory history) {
        return new LoginHistoryResponse(
                history.getLoginHistoryId(),
                history.getIpAddress(),
                history.getCountry(),
                history.getCity(),
                history.isSuspicious(),
                history.getCreatedAt()
        );
    }
}
