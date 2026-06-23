package com.wanted.codebombalms.badge.application.port;

public interface BadgeImageStoragePort {

    StoredBadgeImage upload(
            String originalFileName,
            String contentType,
            long fileSize,
            byte[] imageBytes
    );

    String generateAccessUrl(String objectName);

    void delete(String objectName);

    record StoredBadgeImage(
            String originalFileName,
            String objectName,
            String contentType,
            long fileSize
    ) {
    }
}
