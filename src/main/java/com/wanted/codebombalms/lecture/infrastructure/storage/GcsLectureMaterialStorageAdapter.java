package com.wanted.codebombalms.lecture.infrastructure.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageClientFactory;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageProperties;
import com.wanted.codebombalms.lecture.application.port.LectureMaterialStoragePort;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GcsLectureMaterialStorageAdapter implements LectureMaterialStoragePort {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;
    private static final long DOWNLOAD_URL_DURATION_MINUTES = 1;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "text/csv",
            "application/zip"
    );

    private final GcpStorageProperties properties;
    private final GcpStorageClientFactory storageClientFactory;
    private volatile Storage storage;

    @Override
    public StoredLectureMaterial upload(
            String originalFileName,
            String contentType,
            long fileSize,
            byte[] content
    ) {
        validateFile(originalFileName, contentType, fileSize, content);

        String sanitizedFileName = sanitizeFileName(originalFileName);
        String storedFileName = UUID.randomUUID() + "_" + sanitizedFileName;
        String objectName = buildObjectName(storedFileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(
                        BlobId.of(properties.getStorage().getBucket(), objectName)
                )
                .setContentType(contentType)
                .build();

        try {
            getStorage().create(blobInfo, content);
            return new StoredLectureMaterial(
                    sanitizedFileName,
                    storedFileName,
                    objectName,
                    contentType,
                    fileSize
            );
        } catch (Exception e) {
            throw new ExternalServiceException(
                    LectureErrorCode.LECTURE_MATERIAL_UPLOAD_FAILED,
                    e
            );
        }
    }

    @Override
    public void delete(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        try {
            getStorage().delete(BlobId.of(properties.getStorage().getBucket(), filePath));
        } catch (Exception e) {
            throw new ExternalServiceException(
                    LectureErrorCode.LECTURE_MATERIAL_DELETE_FAILED,
                    e
            );
        }
    }

    @Override
    public String generateDownloadUrl(String filePath, String originalFileName) {
        if (filePath == null || filePath.isBlank()
                || originalFileName == null || originalFileName.isBlank()) {
            throw new ValidationException(LectureErrorCode.LECTURE_MATERIAL_INVALID_FILE);
        }

        String contentDisposition = ContentDisposition.attachment()
                .filename(originalFileName, StandardCharsets.UTF_8)
                .build()
                .toString();

        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(
                    properties.getStorage().getBucket(),
                    filePath
            ).build();

            return getStorage()
                    .signUrl(
                            blobInfo,
                            DOWNLOAD_URL_DURATION_MINUTES,
                            TimeUnit.MINUTES,
                            Storage.SignUrlOption.withV4Signature(),
                            Storage.SignUrlOption.withQueryParams(Map.of(
                                    "response-content-disposition",
                                    contentDisposition
                            ))
                    )
                    .toString();
        } catch (Exception e) {
            throw new ExternalServiceException(
                    LectureErrorCode.LECTURE_MATERIAL_DOWNLOAD_URL_FAILED,
                    e
            );
        }
    }

    private void validateFile(
            String originalFileName,
            String contentType,
            long fileSize,
            byte[] content
    ) {
        boolean invalidFile = originalFileName == null
                || originalFileName.isBlank()
                || contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(contentType)
                || fileSize <= 0
                || fileSize > MAX_FILE_SIZE
                || content == null
                || content.length == 0
                || fileSize != content.length;

        if (invalidFile) {
            throw new ValidationException(LectureErrorCode.LECTURE_MATERIAL_INVALID_FILE);
        }
    }

    private String buildObjectName(String storedFileName) {
        String prefix = normalizePrefix(properties.getStorage().getLectureMaterialPrefix());
        if (prefix.isBlank()) {
            return storedFileName;
        }
        return prefix + "/" + storedFileName;
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null) {
            return "";
        }
        return prefix.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String sanitizeFileName(String originalFileName) {
        return originalFileName
                .replace("\\", "_")
                .replace("/", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private Storage getStorage() {
        if (storage == null) {
            synchronized (this) {
                if (storage == null) {
                    storage = createStorage();
                }
            }
        }
        return storage;
    }

    private Storage createStorage() {
        try {
            return storageClientFactory.create();
        } catch (Exception e) {
            throw new ExternalServiceException(
                    LectureErrorCode.LECTURE_MATERIAL_UPLOAD_FAILED,
                    e
            );
        }
    }

}
