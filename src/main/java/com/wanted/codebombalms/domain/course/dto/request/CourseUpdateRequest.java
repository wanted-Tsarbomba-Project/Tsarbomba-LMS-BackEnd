package com.wanted.codebombalms.domain.course.dto.request;

import com.wanted.codebombalms.domain.course.enums.CourseStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CourseUpdateRequest {

    @Size(max = 100, message = "강좌 제목은 100자 이하로 입력해야 합니다.")
    private String title;

    private String description;

    @Size(max = 500, message = "썸네일 URL은 500자 이하로 입력해야 합니다.")
    private String thumbnailUrl;

    private CourseStatus status;
}