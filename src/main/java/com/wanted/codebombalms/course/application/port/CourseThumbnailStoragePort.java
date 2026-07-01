package com.wanted.codebombalms.course.application.port;

public interface CourseThumbnailStoragePort {

    StoredCourseThumbnail upload(
            String originalFileName,
            String contentType,
            long fileSize,
            byte[] imageBytes
    );

    void delete(String thumbnailUrl);

    record StoredCourseThumbnail(
            String originalFileName,
            String objectName,
            String contentType,
            long fileSize,
            String accessUrl
    ) {
    }
}
