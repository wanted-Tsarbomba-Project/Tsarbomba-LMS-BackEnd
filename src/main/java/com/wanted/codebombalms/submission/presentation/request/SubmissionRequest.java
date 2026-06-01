package com.wanted.codebombalms.submission.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

public class SubmissionRequest {

    @Schema(
            description = "학생이 작성한 Python 코드",
            example = """
                    result = df.shape
                    """,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String code;

    public String getCode() {
        return code;
    }
}
