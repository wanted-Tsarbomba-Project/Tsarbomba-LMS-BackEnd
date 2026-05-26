package com.wanted.codebombalms.gcsconnection;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GcsConnectionSmokeTest {

    private static final String PROJECT_ID = "project-9eb65e0d-55b9-4a40-878";
    private static final String BUCKET_NAME = "codebombalms";
    private static final String TEST_FOLDER = "gcsConnectionTest";

    // TODO: 테스트 실행 전에 본인 이름으로 변경하세요. 예: "홍길동"
    private static final String TESTER_NAME = "이강욱";

    private static final Path CREDENTIALS_PATH = Path.of(
            "secrets/gcp-storage-key.json"
    );

    @Test
    void canUploadSmokeTestFileToGcsBucket() throws IOException {
        assertThat(Files.exists(CREDENTIALS_PATH))
                .as("GCS credentials file must exist at %s", CREDENTIALS_PATH.toAbsolutePath())
                .isTrue();

        Storage storage = StorageOptions.newBuilder()
                .setProjectId(PROJECT_ID)
                .setCredentials(loadCredentials())
                .build()
                .getService();

        assertThat(TESTER_NAME)
                .as("TESTER_NAME must be changed to your name before running this test")
                .isNotEqualTo("YOUR_NAME");

        String objectName = TEST_FOLDER + "/" + TESTER_NAME + "-gcs-connection-" + Instant.now().toEpochMilli() + ".txt";
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, objectName))
                .setContentType("text/plain")
                .build();

        Blob uploadedBlob = storage.create(
                blobInfo,
                "gcs connection smoke test".getBytes(StandardCharsets.UTF_8)
        );

        assertThat(uploadedBlob)
                .as("Smoke test file must be uploaded to gs://%s/%s", BUCKET_NAME, objectName)
                .isNotNull();
    }

    private GoogleCredentials loadCredentials() throws IOException {
        try (InputStream inputStream = Files.newInputStream(CREDENTIALS_PATH)) {
            return GoogleCredentials.fromStream(inputStream);
        }
    }
}

