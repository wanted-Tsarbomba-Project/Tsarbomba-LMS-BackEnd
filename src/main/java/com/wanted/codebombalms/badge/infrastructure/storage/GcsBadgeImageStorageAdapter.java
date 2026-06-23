package com.wanted.codebombalms.badge.infrastructure.storage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.wanted.codebombalms.badge.application.port.BadgeImageStoragePort;
import com.wanted.codebombalms.badge.exception.BadgeErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.infrastructure.cache.CacheNames;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageProperties;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class GcsBadgeImageStorageAdapter implements BadgeImageStoragePort {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final long ACCESS_URL_DURATION_MINUTES = 10;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final GcpStorageProperties properties;
    private final ResourceLoader resourceLoader;

    private volatile Storage storage;

    public GcsBadgeImageStorageAdapter(
            GcpStorageProperties properties,
            ResourceLoader resourceLoader
    ) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public StoredBadgeImage upload(
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
        String objectName = buildObjectName(
                UUID.randomUUID() + "-" + sanitizedFileName
        );

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

            return new StoredBadgeImage(
                    sanitizedFileName,
                    objectName,
                    contentType,
                    fileSize
            );
        } catch (Exception e) {
            throw new ExternalServiceException(
                    BadgeErrorCode.BADGE_IMAGE_UPLOAD_FAILED,
                    e
            );
        }
    }

    @Cacheable(
            cacheNames = CacheNames.BADGE_IMAGE_ACCESS_URL,
            key = "T(com.wanted.codebombalms.badge.infrastructure.cache.BadgeCacheKeys).badgeImageAccessUrl(#objectName)",
            condition = "#objectName != null && !#objectName.isBlank()",
            unless = "#result == null"
    )
    @Override
    public String generateAccessUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            throw new ValidationException(
                    BadgeErrorCode.BADGE_IMAGE_INVALID_FILE
            );
        }

        return createAccessUrl(objectName);
    }

    private String createAccessUrl(String objectName) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(
                    properties.getStorage().getBucket(),
                    objectName
            ).build();

            return getStorage()
                    .signUrl(
                            blobInfo,
                            ACCESS_URL_DURATION_MINUTES,
                            TimeUnit.MINUTES,
                            Storage.SignUrlOption.withV4Signature()
                    )
                    .toString();
        } catch (Exception e) {
            throw new ExternalServiceException(
                    BadgeErrorCode.BADGE_IMAGE_ACCESS_URL_FAILED,
                    e
            );
        }
    }

    @CacheEvict(
            cacheNames = CacheNames.BADGE_IMAGE_ACCESS_URL,
            key = "T(com.wanted.codebombalms.badge.infrastructure.cache.BadgeCacheKeys).badgeImageAccessUrl(#objectName)",
            condition = "#objectName != null && !#objectName.isBlank()"
    )
    @Override
    public void delete(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return;
        }

        try {
            getStorage().delete(
                    BlobId.of(
                            properties.getStorage().getBucket(),
                            objectName
                    )
            );
        } catch (Exception e) {
            throw new ExternalServiceException(
                    BadgeErrorCode.BADGE_IMAGE_DELETE_FAILED,
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
                    BadgeErrorCode.BADGE_IMAGE_INVALID_FILE
            );
        }
    }

    private String buildObjectName(String fileName) {
        String prefix = normalizePrefix(
                properties.getStorage().getBadgeImagePrefix()
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
                .replace("/", "_");
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
                    BadgeErrorCode.BADGE_IMAGE_ACCESS_URL_FAILED,
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
