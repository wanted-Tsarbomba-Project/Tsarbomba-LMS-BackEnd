package com.wanted.codebombalms.course.infrastructure.storage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.wanted.codebombalms.course.application.port.CourseThumbnailStoragePort;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageProperties;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class GcsCourseThumbnailStorageAdapter implements CourseThumbnailStoragePort {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final GcpStorageProperties properties;
    private final ResourceLoader resourceLoader;

    private volatile Storage storage;

    public GcsCourseThumbnailStorageAdapter(
            GcpStorageProperties properties,
            ResourceLoader resourceLoader
    ) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public StoredCourseThumbnail upload(
            String originalFileName,
            String contentType,
            long fileSize,
            byte[] imageBytes
    ) {
        validateImage(
                originalFileName,
                contentType,
                fileSize,
                imageBytes
        );

        String sanitizedFileName = sanitizeFileName(originalFileName);
        String objectName = buildObjectName(UUID.randomUUID() + "-" + sanitizedFileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(
                        BlobId.of(
                                properties.getStorage().getBucket(),
                                objectName
                        )
                )
                .setContentType(contentType)
                .build();

        try {
            getStorage().create(blobInfo, imageBytes);

            return new StoredCourseThumbnail(
                    sanitizedFileName,
                    objectName,
                    contentType,
                    fileSize,
                    buildPublicUrl(objectName)
            );
        } catch (Exception e) {
            throw new ExternalServiceException(
                    CourseErrorCode.COURSE_THUMBNAIL_UPLOAD_FAILED,
                    e
            );
        }
    }

    private void validateImage(
            String originalFileName,
            String contentType,
            long fileSize,
            byte[] imageBytes
    ) {
        boolean invalidFile = originalFileName == null
                || originalFileName.isBlank()
                || contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(contentType)
                || fileSize <= 0
                || fileSize > MAX_FILE_SIZE
                || imageBytes == null
                || imageBytes.length == 0;

        if (invalidFile) {
            throw new ValidationException(
                    CourseErrorCode.COURSE_THUMBNAIL_INVALID_FILE
            );
        }
    }

    private String buildObjectName(String fileName) {
        String prefix = normalizePrefix(
                properties.getStorage().getCourseThumbnailPrefix()
        );

        if (prefix.isBlank()) {
            return fileName;
        }

        return prefix + "/" + fileName;
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null) {
            return "";
        }

        return prefix
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");
    }

    private String sanitizeFileName(String originalFileName) {
        return originalFileName
                .replace("\\", "_")
                .replace("/", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String buildPublicUrl(String objectName) {
        String encodedObjectName = URLEncoder.encode(
                objectName,
                StandardCharsets.UTF_8
        ).replace("+", "%20").replace("%2F", "/");

        return "https://storage.googleapis.com/"
                + properties.getStorage().getBucket()
                + "/"
                + encodedObjectName;
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
            return StorageOptions.newBuilder()
                    .setProjectId(properties.getProjectId())
                    .setCredentials(loadCredentials())
                    .build()
                    .getService();
        } catch (Exception e) {
            throw new ExternalServiceException(
                    CourseErrorCode.COURSE_THUMBNAIL_UPLOAD_FAILED,
                    e
            );
        }
    }

    private GoogleCredentials loadCredentials() throws IOException {
        Resource resource = resourceLoader.getResource(
                properties.getCredentials().getLocation()
        );

        try (InputStream inputStream = resource.getInputStream()) {
            return GoogleCredentials.fromStream(inputStream);
        }
    }
}
