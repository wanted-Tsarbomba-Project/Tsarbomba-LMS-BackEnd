package com.wanted.codebombalms.course.infrastructure.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.wanted.codebombalms.course.application.port.CourseThumbnailStoragePort;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageClientFactory;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageProperties;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
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
    private final GcpStorageClientFactory storageClientFactory;

    private volatile Storage storage;

    public GcsCourseThumbnailStorageAdapter(
            GcpStorageProperties properties,
            GcpStorageClientFactory storageClientFactory
    ) {
        this.properties = properties;
        this.storageClientFactory = storageClientFactory;
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

    @Override
    public void delete(String thumbnailUrl) {
        String objectName = extractObjectName(thumbnailUrl);
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
                    CourseErrorCode.COURSE_THUMBNAIL_DELETE_FAILED,
                    e
            );
        }
    }

    private String extractObjectName(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            return null;
        }

        try {
            URI uri = URI.create(thumbnailUrl);
            String expectedHost = "storage.googleapis.com";
            String bucket = properties.getStorage().getBucket();
            String path = uri.getPath();
            String bucketPrefix = "/" + bucket + "/";

            if (!expectedHost.equals(uri.getHost()) || path == null || !path.startsWith(bucketPrefix)) {
                return null;
            }

            String objectName = URLDecoder.decode(
                    path.substring(bucketPrefix.length()),
                    StandardCharsets.UTF_8
            );
            String thumbnailPrefix = normalizePrefix(properties.getStorage().getCourseThumbnailPrefix());
            if (!thumbnailPrefix.isBlank() && !objectName.startsWith(thumbnailPrefix + "/")) {
                return null;
            }

            return objectName;
        } catch (IllegalArgumentException e) {
            return null;
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
                || imageBytes.length == 0
                || fileSize != imageBytes.length
                || !hasValidImageSignature(contentType, imageBytes);

        if (invalidFile) {
            throw new ValidationException(
                    CourseErrorCode.COURSE_THUMBNAIL_INVALID_FILE
            );
        }
    }

    private boolean hasValidImageSignature(
            String contentType,
            byte[] imageBytes
    ) {
        return switch (contentType) {
            case "image/jpeg" -> isJpeg(imageBytes);
            case "image/png" -> isPng(imageBytes);
            case "image/webp" -> isWebp(imageBytes);
            default -> false;
        };
    }

    private boolean isJpeg(byte[] imageBytes) {
        return imageBytes.length >= 3
                && (imageBytes[0] & 0xFF) == 0xFF
                && (imageBytes[1] & 0xFF) == 0xD8
                && (imageBytes[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] imageBytes) {
        byte[] pngSignature = new byte[] {
                (byte) 0x89,
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A
        };

        return startsWith(imageBytes, pngSignature);
    }

    private boolean isWebp(byte[] imageBytes) {
        return imageBytes.length >= 12
                && imageBytes[0] == 'R'
                && imageBytes[1] == 'I'
                && imageBytes[2] == 'F'
                && imageBytes[3] == 'F'
                && imageBytes[8] == 'W'
                && imageBytes[9] == 'E'
                && imageBytes[10] == 'B'
                && imageBytes[11] == 'P';
    }

    private boolean startsWith(
            byte[] imageBytes,
            byte[] signature
    ) {
        if (imageBytes.length < signature.length) {
            return false;
        }

        for (int index = 0; index < signature.length; index++) {
            if (imageBytes[index] != signature[index]) {
                return false;
            }
        }

        return true;
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
            return storageClientFactory.create();
        } catch (Exception e) {
            throw new ExternalServiceException(
                    CourseErrorCode.COURSE_THUMBNAIL_UPLOAD_FAILED,
                    e
            );
        }
    }

}
