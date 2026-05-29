package com.wanted.codebombalms.badge.application.command;

public record UpdateBadgeCommand(
        String badgeName,
        String description,
        Integer requiredPoint,
        String status,
        String originalFileName,
        String contentType,
        long fileSize,
        byte[] imageBytes
) {
    public boolean hasNewImage() {
        return imageBytes != null && imageBytes.length > 0;
    }
}
