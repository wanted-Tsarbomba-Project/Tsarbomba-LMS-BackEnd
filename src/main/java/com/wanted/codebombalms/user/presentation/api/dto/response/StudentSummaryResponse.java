package com.wanted.codebombalms.user.presentation.api.dto.response;

import com.wanted.codebombalms.user.application.query.StudentSummary;

import java.time.LocalDateTime;

public record StudentSummaryResponse(
        Long userId,
        String email,
        String nickname,
        boolean isLocked,
        LocalDateTime createdAt
) {

    public static StudentSummaryResponse from(StudentSummary summary) {
        return new StudentSummaryResponse(
                summary.userId(),
                summary.email(),
                summary.nickname(),
                summary.isLocked(),
                summary.createdAt()
        );
    }
}
