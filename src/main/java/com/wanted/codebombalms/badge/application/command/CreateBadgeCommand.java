package com.wanted.codebombalms.badge.application.command;

public record CreateBadgeCommand(
        String badgeName,
        String description,
        Integer requiredPoint,
        String originalFileName,
        String contentType,
        long fileSize,
        byte[] imageBytes
) {
}
