package com.wanted.codebombalms.lecture.presentation.api.request;

import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import jakarta.validation.constraints.Size;

public record LectureUpdateRequest(
        @Size(max = 100, message = "강의 제목은 100자 이하로 입력해야 합니다.")
        String title,

        String description,

        @Size(max = 500, message = "영상 URL은 500자 이하로 입력해야 합니다.")
        String videoUrl,

        @Size(max = 500, message = "썸네일 URL은 500자 이하로 입력해야 합니다.")
        String thumbnailUrl,

        Long problemCategoryId,

        Integer lectureOrder,

        LectureStatus status
) {
}
