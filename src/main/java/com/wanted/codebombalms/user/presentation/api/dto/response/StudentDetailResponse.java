package com.wanted.codebombalms.user.presentation.api.dto.response;

import com.wanted.codebombalms.user.application.query.StudentDetail;

import java.time.LocalDateTime;

public record StudentDetailResponse(
        Long userId,
        String email,
        String name,
        String nickname,
        String phone,
        String role,
        String provider,
        boolean isLocked,
        LocalDateTime createdAt
) {

    public static StudentDetailResponse from(StudentDetail detail) {
        return new StudentDetailResponse(
                detail.userId(),
                detail.email(),
                detail.name(),
                detail.nickname(),
                detail.phone(),
                detail.role().name(),
                detail.provider().name(),
                detail.isLocked(),
                detail.createdAt()
        );
    }
}
