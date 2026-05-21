package com.wanted.codebombalms.course.presentation.api.request;

import com.wanted.codebombalms.course.domain.model.CourseStatus;
import jakarta.validation.constraints.Size;

public record CourseUpdateRequest(
        @Size(max = 100, message = "강좌 제목은 100자 이하로 입력해야 합니다.")
        String title,

        String description,

        @Size(max = 500, message = "썸네일 URL은 500자 이하로 입력해야 합니다.")
        String thumbnailUrl,

        CourseStatus status
) {
}
