package com.wanted.codebombalms.problems.dataset.infrastructure.persistence;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.problems.dataset.application.port.GenerateDatasetAccessUrlPort;
import com.wanted.codebombalms.problems.dataset.infrastructure.storage.GcpStorageProperties;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Component
public class GcsDatasetAccessUrlAdapter implements GenerateDatasetAccessUrlPort {

    private static final long SIGNED_URL_DURATION_MINUTES = 30;
    private final Storage storage;
    private final GcpStorageProperties properties;
    private final ResourceLoader resourceLoader;

    public GcsDatasetAccessUrlAdapter(
            GcpStorageProperties properties,
            ResourceLoader resourceLoader
    ) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;

        try {
            this.storage = createStorage();
        } catch (IOException e) {
            throw new ExternalServiceException(
                    ProblemErrorCode.PROBLEM_DATASET_ACCESS_URL_FAILED,e
            );
        }
    }

    @Override
    public String generate(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return null;
        }

        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(
                    properties.getStorage().getBucket(),
                    filePath
            ).build();

            return storage
                    .signUrl(
                            blobInfo,
                            SIGNED_URL_DURATION_MINUTES,
                            TimeUnit.MINUTES,
                            Storage.SignUrlOption.withV4Signature()
                    )
                    .toString();
        } catch (Exception e) {
            throw new ExternalServiceException(ProblemErrorCode.PROBLEM_DATASET_ACCESS_URL_FAILED, e);
        }
    }

    private Storage createStorage() throws IOException {
        return StorageOptions.newBuilder()
                .setProjectId(properties.getProjectId())
                .setCredentials(loadCredentials())
                .build()
                .getService();
    }

    private GoogleCredentials loadCredentials() throws IOException {
        Resource resource = resourceLoader.getResource(properties.getCredentials().getLocation());

        try (InputStream inputStream = resource.getInputStream()) {
            return GoogleCredentials.fromStream(inputStream);
        }
    }
}
