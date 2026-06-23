package com.wanted.codebombalms.course.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
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
}
