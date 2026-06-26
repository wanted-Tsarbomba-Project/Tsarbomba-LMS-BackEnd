package com.wanted.codebombalms.global.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.StorageOptions;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class GcsConnectionTest {

    private static final String TESTER_NAME = "이강욱";
    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Test
    @DisplayName("서비스 계정 키로 GCS에 연결하고 테스트 파일을 업로드할 수 있다")
    @EnabledIfEnvironmentVariable(named = "RUN_GCS_CONNECTION_TEST", matches = "true")
    void uploadConnectionTestFile() throws Exception {
        GcsConfig config = loadGcsConfig();
        String objectName = "gcsConnectionTest/"
                + TESTER_NAME
                + "-gcs-connection-"
                + LocalDateTime.now().format(FILE_TIME_FORMATTER)
                + ".txt";

        var storage = StorageOptions.newBuilder()
                .setProjectId(config.projectId())
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build()
                .getService();

        BlobInfo blobInfo = BlobInfo.newBuilder(
                        BlobId.of(config.bucket(), objectName)
                )
                .setContentType("text/plain; charset=utf-8")
                .build();

        storage.create(
                blobInfo,
                ("GCS connection test: " + TESTER_NAME).getBytes(StandardCharsets.UTF_8)
        );

        assertThat(storage.get(config.bucket(), objectName)).isNotNull();
        System.out.println("GCS connection test uploaded: gs://" + config.bucket() + "/" + objectName);
    }

    private GcsConfig loadGcsConfig() throws Exception {
        List<String> lines = Files.readAllLines(Path.of("src/main/resources/application.yml"));

        String projectId = resolvePlaceholder(findValue(lines, "project-id:"));
        String bucket = resolvePlaceholder(findValue(lines, "bucket:"));

        return new GcsConfig(
                projectId,
                bucket
        );
    }

    private String findValue(List<String> lines, String key) {
        return lines.stream()
                .map(String::trim)
                .filter(line -> line.startsWith(key))
                .map(line -> line.substring(key.length()).trim())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing GCS config: " + key));
    }

    private String resolvePlaceholder(String value) {
        if (!value.startsWith("${") || !value.endsWith("}")) {
            return value;
        }

        String expression = value.substring(2, value.length() - 1);
        String[] parts = expression.split(":", 2);
        String envValue = System.getenv(parts[0]);

        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        if (parts.length == 2) {
            return parts[1];
        }

        throw new IllegalStateException("Missing environment variable: " + parts[0]);
    }

    private record GcsConfig(
            String projectId,
            String bucket
    ) {
    }
}
