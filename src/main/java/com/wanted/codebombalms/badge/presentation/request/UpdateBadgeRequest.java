package com.wanted.codebombalms.badge.presentation.request;

public record UpdateBadgeRequest(
        String badgeName,
        String description,
        Integer requiredPoint,
        String status
) {
}
