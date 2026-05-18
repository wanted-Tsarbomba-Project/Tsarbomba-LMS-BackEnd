package com.wanted.codebombalms.domain.lecture.dto.request;

import com.wanted.codebombalms.domain.lecture.enums.LectureStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class LectureCreateRequest {

    @NotBlank(message = "강의 제목은 필수입니다.")
    @Size(max = 100, message = "강의 제목은 100자 이하로 입력해야 합니다.")
    private String title;

    private String description;

    @Size(max = 500, message = "영상 URL은 500자 이하로 입력해야 합니다.")
    private String videoUrl;

    @Size(max = 500, message = "썸네일 URL은 500자 이하로 입력해야 합니다.")
    private String thumbnailUrl;

    @NotNull(message = "강의 순서는 필수입니다.")
    private Integer lectureOrder;

    private LectureStatus status;
}