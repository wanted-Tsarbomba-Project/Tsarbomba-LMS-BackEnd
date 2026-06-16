package com.wanted.codebombalms.problems.dataset.infrastructure.storage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageProperties;
import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.port.StoreDatasetFilePort;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Primary
@Component
@Slf4j
public class GcsDatasetFileStorage implements StoreDatasetFilePort {

    private static final String CSV_CONTENT_TYPE = "text/csv";
    private static final String STORAGE_PUBLIC_URL = "https://storage.googleapis.com";

    private final GcpStorageProperties properties;
    private final ResourceLoader resourceLoader;

    public GcsDatasetFileStorage(
            GcpStorageProperties properties,
            ResourceLoader resourceLoader
    ) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public StoredDatasetFile store(UploadProblemDatasetCommand command) throws IOException {
        String originalFileName = sanitizeFileName(command.originalFileName());
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;
        String objectName = buildObjectName(storedFileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(
                        BlobId.of(properties.getStorage().getBucket(), objectName)
                )
                .setContentType(CSV_CONTENT_TYPE)
                .build();

        createStorage().create(blobInfo, command.content());

        return StoredDatasetFile.create(
                originalFileName,
                storedFileName,
                buildFileUrl(objectName),
                objectName,
                command.fileSize()
        );
    }

    private Storage createStorage() throws IOException {
        return StorageOptions.newBuilder()
                .setProjectId(properties.getProjectId())
                .setCredentials(loadCredentials())
                .build()
                .getService();
    }

    @Override
    public void delete(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        try {
            createStorage().delete(
                    BlobId.of(properties.getStorage().getBucket(), filePath)
            );
        } catch (Exception e) {
            log.warn("GCS에서 파일 삭제를 실패했습니다. filePath={}", filePath, e);
        }
    }

    private GoogleCredentials loadCredentials() throws IOException {
        Resource resource = resourceLoader.getResource(properties.getCredentials().getLocation());
        try (InputStream inputStream = resource.getInputStream()) {
            return GoogleCredentials.fromStream(inputStream);
        }
    }

    private String buildObjectName(String storedFileName) {
        String prefix = normalizePrefix(properties.getStorage().getDatasetPrefix());
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

    private String buildFileUrl(String objectName) {
        return STORAGE_PUBLIC_URL + "/"
                + properties.getStorage().getBucket()
                + "/"
                + objectName;
    }

    private String sanitizeFileName(String originalFileName) {
        return originalFileName
                .replace("\\", "_")
                .replace("/", "_");
    }
}
