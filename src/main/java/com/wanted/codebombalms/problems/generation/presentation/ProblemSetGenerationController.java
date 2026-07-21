package com.wanted.codebombalms.problems.generation.presentation;


import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.generation.application.result.ProblemSetDraftResult;
import com.wanted.codebombalms.problems.generation.application.usecase.GenerateProblemSetDraftUseCase;
import com.wanted.codebombalms.problems.generation.presentation.api.request.ProblemSetDraftGenerateRequest;
import com.wanted.codebombalms.problems.generation.presentation.api.request.ProblemSetDraftGenerateSwaggerRequest;
import com.wanted.codebombalms.problems.generation.presentation.api.response.ProblemSetDraftGenerateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(
        name = "문제세트 AI 초안 생성",
        description = "관리자가 데이터셋 기반 문제세트 초안을 AI로 생성하고, 최종 등록 전 검토할 수 있는 API"
)
@RestController
@RequiredArgsConstructor
public class ProblemSetGenerationController {

    private final GenerateProblemSetDraftUseCase generateProblemSetDraftUseCase;

    @Operation(
            summary = "AI 문제세트 초안 생성",
            description = """
                    관리자가 입력한 문제 생성 방향, 난이도, 카테고리, 데이터셋 정보를 기반으로 문제세트 초안을 생성합니다.

                    이 API는 실제 문제세트를 DB에 저장하지 않습니다.
                    생성된 초안은 프론트에서 미리보기로 보여준 뒤, 관리자가 반영하기를 누르면 문제 등록 폼에 채워집니다.
                    최종 저장은 기존 문제 등록 API를 사용합니다.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = ProblemSetDraftGenerateSwaggerRequest.class),
                    encoding = {
                            @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE),
                            @Encoding(name = "datasetFile", contentType = "text/csv")
                    }
            )
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping(
            value = "/api/v1/admin/problem-set-drafts/generate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ProblemSetDraftGenerateResponse>> generateProblemSetDraft(
            @AuthenticationPrincipal Long userId,
            @RequestPart("request") @Valid ProblemSetDraftGenerateRequest request,
            @RequestPart("datasetFile") MultipartFile datasetFile
    ) {
        ProblemSetDraftResult result = generateProblemSetDraftUseCase.generate(
                userId,
                request,
                datasetFile
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                ProblemSetDraftGenerateResponse.from(result)
        ));
    }
}
