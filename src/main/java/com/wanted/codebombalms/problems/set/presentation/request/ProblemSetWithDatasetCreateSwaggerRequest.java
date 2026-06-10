package com.wanted.codebombalms.problems.set.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "데이터셋 파일을 포함한 문제 세트 등록 multipart 요청")
public record ProblemSetWithDatasetCreateSwaggerRequest(
        @Schema(
                description = "문제와 problems[].testCases[] 목록을 포함한 문제 세트 등록 JSON 파트",
                implementation = ProblemSetCreateRequest.class,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        ProblemSetCreateRequest request,

        @Schema(
                description = "문제 세트에 연결할 CSV 데이터셋 파일",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        MultipartFile datasetFile
) {
}
