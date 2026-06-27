package com.wanted.codebombalms.global.infrastructure.storage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GcpStorageClientFactory {

    private final GcpStorageProperties properties;

    public Storage create() throws IOException {
        return StorageOptions.newBuilder()
                .setProjectId(properties.getProjectId())
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build()
                .getService();
    }
}
