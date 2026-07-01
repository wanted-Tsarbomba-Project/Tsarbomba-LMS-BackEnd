package com.wanted.codebombalms.course.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageClientFactory;
import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GcsCourseThumbnailStorageAdapter test")
class GcsCourseThumbnailStorageAdapterTest {

    private final GcsCourseThumbnailStorageAdapter storageAdapter =
            new GcsCourseThumbnailStorageAdapter(null, null);

    @Test
    void upload_throwsValidation_whenContentTypeIsImageButBytesAreNotImage() {
        byte[] fakeImageBytes = "not image".getBytes();

        assertThatThrownBy(() -> storageAdapter.upload(
                "thumbnail.png",
                "image/png",
                fakeImageBytes.length,
                fakeImageBytes
        ))
                .isInstanceOf(ValidationException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.COURSE_THUMBNAIL_INVALID_FILE);
    }

    @Test
    void upload_throwsValidation_whenFileSizeDoesNotMatchByteLength() {
        byte[] pngBytes = new byte[] {
                (byte) 0x89,
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A
        };

        assertThatThrownBy(() -> storageAdapter.upload(
                "thumbnail.png",
                "image/png",
                pngBytes.length + 1,
                pngBytes
        ))
                .isInstanceOf(ValidationException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.COURSE_THUMBNAIL_INVALID_FILE);
    }

    @Test
    void delete_deletesGcsObject_whenUrlBelongsToConfiguredBucket() throws Exception {
        Storage storage = mock(Storage.class);
        GcpStorageClientFactory storageClientFactory = mock(GcpStorageClientFactory.class);
        GcpStorageProperties properties = createProperties("codebombalms");
        GcsCourseThumbnailStorageAdapter adapter =
                new GcsCourseThumbnailStorageAdapter(properties, storageClientFactory);

        when(storageClientFactory.create()).thenReturn(storage);

        adapter.delete("https://storage.googleapis.com/codebombalms/course_thumbnail_images/java.png");

        verify(storage).delete(BlobId.of("codebombalms", "course_thumbnail_images/java.png"));
    }

    @Test
    void delete_ignoresUrl_whenUrlDoesNotBelongToConfiguredBucket() throws Exception {
        GcpStorageClientFactory storageClientFactory = mock(GcpStorageClientFactory.class);
        GcpStorageProperties properties = createProperties("codebombalms");
        GcsCourseThumbnailStorageAdapter adapter =
                new GcsCourseThumbnailStorageAdapter(properties, storageClientFactory);

        adapter.delete("https://example.com/images/java.png");

        verify(storageClientFactory, never()).create();
    }

    private GcpStorageProperties createProperties(String bucket) {
        GcpStorageProperties properties = new GcpStorageProperties();
        properties.getStorage().setBucket(bucket);
        return properties;
    }
}
