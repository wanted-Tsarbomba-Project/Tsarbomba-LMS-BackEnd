package com.wanted.codebombalms.badge.presentation.request;

public record CreateBadgeRequest(
        String badgeName,
        String description,
        Integer requiredPoint
) {
}
