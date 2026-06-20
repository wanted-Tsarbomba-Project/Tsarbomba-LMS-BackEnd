package com.wanted.codebombalms.lecture.application.port;

public interface LectureMaterialStoragePort {

    StoredLectureMaterial upload(
            String originalFileName,
            String contentType,
            long fileSize,
            byte[] content
    );

    void delete(String filePath);

    String generateDownloadUrl(String filePath, String originalFileName);

    record StoredLectureMaterial(
            String originalFileName,
            String storedFileName,
            String filePath,
            String contentType,
            long fileSize
    ) {
    }
}
