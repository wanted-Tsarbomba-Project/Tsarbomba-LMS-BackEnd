package com.wanted.codebombalms.problems.generation.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "AI problem set draft generation multipart request")
public record ProblemSetDraftGenerateSwaggerRequest(
        @Schema(
                description = "Problem set draft generation JSON part",
                implementation = ProblemSetDraftGenerateRequest.class,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        ProblemSetDraftGenerateRequest request,

        @Schema(
                description = "CSV dataset file used for draft generation",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        MultipartFile datasetFile
) {
}
