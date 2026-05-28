package com.wanted.codebombalms.submission.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

public class SubmissionRequest {

    @Schema(description = "코드를 제출하는 학생 ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(
            description = "학생이 작성한 Python 코드",
            example = """
                    result = df.shape
                    """,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String code;

    public Long getUserId() {
        return userId;
    }

    public String getCode() {
        return code;
    }
}
