package com.wanted.codebombalms.global.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.StorageOptions;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class GcsConnectionTest {

    private static final String RUN_TEST_ENV = "RUN_GCS_CONNECTION_TEST";
    private static final String PROJECT_ID_ENV = "GCS_CONNECTION_TEST_PROJECT_ID";
    private static final String BUCKET_ENV = "GCS_CONNECTION_TEST_BUCKET";
    private static final String TEST_OBJECT_PREFIX = "gcs-connection-test/";

    @Test
    @DisplayName("ADC 인증으로 GCS 테스트 버킷에 파일을 업로드하고 삭제할 수 있다")
    @EnabledIfEnvironmentVariable(named = RUN_TEST_ENV, matches = "true")
    void uploadConnectionTestFile() throws Exception {
        GcsConfig config = loadGcsConfig();
        String objectName = TEST_OBJECT_PREFIX
                + UUID.randomUUID()
                + ".txt";

        var storage = StorageOptions.newBuilder()
                .setProjectId(config.projectId())
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build()
                .getService();

        BlobId blobId = BlobId.of(config.bucket(), objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("text/plain; charset=utf-8")
                .build();

        try {
            storage.create(
                    blobInfo,
                    "GCS connection test".getBytes(StandardCharsets.UTF_8)
            );

            assertThat(storage.get(blobId)).isNotNull();
        } finally {
            storage.delete(blobId);
        }
    }

    private GcsConfig loadGcsConfig() {
        return new GcsConfig(
                requiredEnv(PROJECT_ID_ENV),
                requiredEnv(BUCKET_ENV)
        );
    }

    private String requiredEnv(String key) {
        String value = System.getenv(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable: " + key);
        }

        return value;
    }

    private record GcsConfig(
            String projectId,
            String bucket
    ) {
    }
}
