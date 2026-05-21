package com.wanted.codebombalms.enrollment.presentation.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EnrollmentCreateRequest {

    @NotNull(message = "학생 ID는 필수입니다.")
    private Long studentId;
}