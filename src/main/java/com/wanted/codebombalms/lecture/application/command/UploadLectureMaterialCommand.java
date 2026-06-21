package com.wanted.codebombalms.lecture.application.command;

public record UploadLectureMaterialCommand(
        Long lectureId,
        String originalFileName,
        String contentType,
        long fileSize,
        byte[] content
) {
}
