package com.wanted.codebombalms.domain.course.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseCreateRequest(
    @NotNull(message = "강사 ID는 필수입니다.")
    Long instructorId,

    @NotBlank(message = "강좌 제목은 필수입니다.")
    @Size(max = 100, message = "강좌 제목은 100자 이하로 입력해야 합니다.")
    String title,

    String description,

    @Size(max = 500, message = "썸네일 URL은 500자 이하로 입력해야 합니다.")
    String thumbnailUrl
) {
}
