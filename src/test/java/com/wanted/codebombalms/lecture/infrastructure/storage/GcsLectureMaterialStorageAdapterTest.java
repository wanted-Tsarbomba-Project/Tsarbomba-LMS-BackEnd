package com.wanted.codebombalms.lecture.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageClientFactory;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageProperties;
import com.wanted.codebombalms.lecture.application.port.LectureMaterialStoragePort.StoredLectureMaterial;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import java.nio.charset.StandardCharsets;
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

    @Test
    void upload_preservesOriginalKoreanFileNameWithUnicodeEscapes() throws Exception {
        Storage storage = mock(Storage.class);
        GcpStorageClientFactory storageClientFactory = mock(GcpStorageClientFactory.class);
        GcpStorageProperties properties = createProperties("codebombalms");
        GcsLectureMaterialStorageAdapter adapter =
                new GcsLectureMaterialStorageAdapter(properties, storageClientFactory);
        byte[] content = "lecture material".getBytes(StandardCharsets.UTF_8);
        String originalFileName = "\uC790\uB8CC 1\uAC15.pdf";

        when(storageClientFactory.create()).thenReturn(storage);
        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(mock(Blob.class));

        StoredLectureMaterial storedMaterial = adapter.upload(
                originalFileName,
                "application/pdf",
                content.length,
                content
        );

        assertThat(storedMaterial.originalFileName()).isEqualTo(originalFileName);
        assertThat(storedMaterial.storedFileName()).endsWith("___1_.pdf");
        assertThat(storedMaterial.filePath()).startsWith("lecture_materials/");
    }

    @Test
    void upload_throwsValidation_whenOriginalFileNameIsBlank() {
        GcsLectureMaterialStorageAdapter adapter = new GcsLectureMaterialStorageAdapter(null, null);
        byte[] content = "lecture material".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> adapter.upload(
                " ",
                "application/pdf",
                content.length,
                content
        ))
                .isInstanceOf(ValidationException.class)
                .extracting("errorCode")
                .isEqualTo(LectureErrorCode.LECTURE_MATERIAL_INVALID_FILE);
    }

    @Test
    void upload_throwsValidation_whenContentTypeIsNotAllowed() {
        GcsLectureMaterialStorageAdapter adapter = new GcsLectureMaterialStorageAdapter(null, null);
        byte[] content = "lecture material".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> adapter.upload(
                "guide.exe",
                "application/x-msdownload",
                content.length,
                content
        ))
                .isInstanceOf(ValidationException.class)
                .extracting("errorCode")
                .isEqualTo(LectureErrorCode.LECTURE_MATERIAL_INVALID_FILE);
    }

    @Test
    void upload_throwsValidation_whenFileSizeExceedsLimit() {
        GcsLectureMaterialStorageAdapter adapter = new GcsLectureMaterialStorageAdapter(null, null);
        byte[] content = "lecture material".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> adapter.upload(
                "guide.pdf",
                "application/pdf",
                20 * 1024 * 1024 + 1L,
                content
        ))
                .isInstanceOf(ValidationException.class)
                .extracting("errorCode")
                .isEqualTo(LectureErrorCode.LECTURE_MATERIAL_INVALID_FILE);
    }

    @Test
    void upload_throwsValidation_whenContentIsEmpty() {
        GcsLectureMaterialStorageAdapter adapter = new GcsLectureMaterialStorageAdapter(null, null);
        byte[] content = new byte[0];

        assertThatThrownBy(() -> adapter.upload(
                "guide.pdf",
                "application/pdf",
                content.length,
                content
        ))
                .isInstanceOf(ValidationException.class)
                .extracting("errorCode")
                .isEqualTo(LectureErrorCode.LECTURE_MATERIAL_INVALID_FILE);
    }

    @Test
    void upload_throwsExternalServiceException_whenStorageCreateFails() throws Exception {
        Storage storage = mock(Storage.class);
        GcpStorageClientFactory storageClientFactory = mock(GcpStorageClientFactory.class);
        GcpStorageProperties properties = createProperties("codebombalms");
        GcsLectureMaterialStorageAdapter adapter =
                new GcsLectureMaterialStorageAdapter(properties, storageClientFactory);
        byte[] content = "lecture material".getBytes(StandardCharsets.UTF_8);

        when(storageClientFactory.create()).thenReturn(storage);
        when(storage.create(any(BlobInfo.class), any(byte[].class)))
                .thenThrow(new RuntimeException("upload failed"));

        assertThatThrownBy(() -> adapter.upload(
                "guide.pdf",
                "application/pdf",
                content.length,
                content
        ))
                .isInstanceOf(ExternalServiceException.class)
                .extracting("errorCode")
                .isEqualTo(LectureErrorCode.LECTURE_MATERIAL_UPLOAD_FAILED);
    }

    private GcpStorageProperties createProperties(String bucket) {
        GcpStorageProperties properties = new GcpStorageProperties();
        properties.getStorage().setBucket(bucket);
        return properties;
    }
}
