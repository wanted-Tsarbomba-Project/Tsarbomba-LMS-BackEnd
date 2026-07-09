package com.wanted.codebombalms.lecture.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageClientFactory;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageProperties;
import com.wanted.codebombalms.lecture.application.port.LectureMaterialStoragePort.StoredLectureMaterial;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("GcsLectureMaterialStorageAdapter test")
class GcsLectureMaterialStorageAdapterTest {

    @Test
    void upload_preservesOriginalKoreanFileNameForDisplayAndSanitizesStoredFileName() throws Exception {
        Storage storage = mock(Storage.class);
        GcpStorageClientFactory storageClientFactory = mock(GcpStorageClientFactory.class);
        GcpStorageProperties properties = createProperties("codebombalms");
        GcsLectureMaterialStorageAdapter adapter =
                new GcsLectureMaterialStorageAdapter(properties, storageClientFactory);
        byte[] content = "lecture material".getBytes();
        String originalFileName = "자료 1강.pdf";

        when(storageClientFactory.create()).thenReturn(storage);
        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(mock(Blob.class));

        StoredLectureMaterial storedMaterial = adapter.upload(
                originalFileName,
                "application/pdf",
                content.length,
                content
        );

        ArgumentCaptor<BlobInfo> blobInfoCaptor = ArgumentCaptor.forClass(BlobInfo.class);
        verify(storage).create(blobInfoCaptor.capture(), any(byte[].class));

        assertThat(storedMaterial.originalFileName()).isEqualTo(originalFileName);
        assertThat(storedMaterial.storedFileName()).endsWith("____1_.pdf");
        assertThat(storedMaterial.filePath()).startsWith("lecture_materials/");
        assertThat(blobInfoCaptor.getValue().getName()).isEqualTo(storedMaterial.filePath());
    }

    private GcpStorageProperties createProperties(String bucket) {
        GcpStorageProperties properties = new GcpStorageProperties();
        properties.getStorage().setBucket(bucket);
        return properties;
    }
}
