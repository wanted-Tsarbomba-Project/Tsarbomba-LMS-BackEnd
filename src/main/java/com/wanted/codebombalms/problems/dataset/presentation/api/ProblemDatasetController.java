package com.wanted.codebombalms.problems.dataset.presentation.api;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.usecase.IssueDatasetDownloadUrlUseCase;
import com.wanted.codebombalms.problems.dataset.application.usecase.UploadProblemDatasetUseCase;
import com.wanted.codebombalms.problems.dataset.presentation.api.response.DatasetDownloadUrlResponse;
import com.wanted.codebombalms.problems.dataset.presentation.api.response.ProblemDatasetUploadResponse;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "문제 데이터셋", description = "코드 실행형 문제에서 사용하는 CSV 데이터셋 업로드 API")
@RestController
@RequiredArgsConstructor
public class ProblemDatasetController {

    private final UploadProblemDatasetUseCase uploadProblemDatasetUseCase;
    private final IssueDatasetDownloadUrlUseCase issueDatasetDownloadUrlUseCase;

    @Operation(
            summary = "데이터셋 단독 업로드",
            description = "CSV 데이터셋 파일을 GCS에 업로드합니다. 문제 세트 등록과 동시에 연결하려면 /api/v1/problems/with-dataset API를 사용합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "데이터셋 업로드 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "업로드 성공",
                                    value = """
                                            {
                                              "timestamp": "2026-05-28T12:00:00",
                                              "status": 201,
                                              "code": "COMMON-CREATED",
                                              "message": "리소스가 성공적으로 생성되었습니다.",
                                              "data": {
                                                "datasetId": 3001,
                                                "originalFileName": "employee_performance.csv",
                                                "status": "ACTIVE"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-DAT-004, PRB-DAT-005 - CSV 파일 오류 또는 업로드 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "CSV 파일 오류",
                                    value = """
                                            {
                                              "timestamp": "2026-05-28T12:00:00",
                                              "status": 400,
                                              "code": "PRB-DAT-004",
                                              "message": "CSV 파일만 업로드할 수 있습니다.",
                                              "path": "/api/v1/problem-datasets"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "AUT-003 - 인증 토큰 만료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인증 토큰 만료",
                                    value = """
                                            {
                                              "timestamp": "2026-05-28T12:00:00",
                                              "status": 401,
                                              "code": "AUT-003",
                                              "message": "만료된 토큰입니다.",
                                              "path": "/api/v1/problem-datasets"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping(
            value = "/api/v1/problem-datasets",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ProblemDatasetUploadResponse>> uploadDataset(
            @Parameter(description = "업로드할 CSV 데이터셋 파일", required = true)
            @RequestParam("datasetFile") MultipartFile datasetFile
    ) {
        var response = new ProblemDatasetUploadResponse(
                uploadProblemDatasetUseCase.handle(toCommand(datasetFile))
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        ApiResponseMessage.CREATED,
                        response
                ));
    }

    private UploadProblemDatasetCommand toCommand(MultipartFile datasetFile) {
        try {
            return new UploadProblemDatasetCommand(
                    datasetFile.getOriginalFilename(),
                    datasetFile.getContentType(),
                    datasetFile.getBytes(),
                    datasetFile.getSize()
            );
        } catch (IOException e) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_UPLOAD_FAILED);
        }
    }

    @Operation(
            summary = "문제세트 데이터셋 다운로드 URL 발급",
            description = """
                로그인한 사용자가 문제세트에 연결된 CSV 데이터셋을 다운로드할 수 있도록
                일정 시간 동안 유효한 Signed URL을 발급합니다.

                다운로드 버튼을 다시 누르면 새로운 Signed URL이 발급됩니다.
                """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "다운로드 URL 발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "다운로드 URL 발급 성공",
                                    value = """
                                        {
                                          "timestamp": "2026-06-14T12:00:00Z",
                                          "status": 200,
                                          "code": "COMMON-SUCCESS",
                                          "message": "데이터셋 다운로드 URL이 발급되었습니다.",
                                          "data": {
                                            "fileName": "employee_performance.csv",
                                            "downloadUrl": "https://storage.googleapis.com/codebombalms/problem_dataset/example.csv?X-Goog-Signature=example"
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-DAT-001 - 활성 데이터셋을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "502",
                    description = "PRB-DAT-007 - Signed URL 생성 실패"
            )
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/v1/problem-sets/{problemSetId}/dataset/download-url")
    public ResponseEntity<ApiResponse<DatasetDownloadUrlResponse>> issueDownloadUrl(
            @Parameter(
                    description = "데이터셋이 연결된 문제세트 ID",
                    required = true,
                    example = "4001"
            )
            @PathVariable Long problemSetId,
            @AuthenticationPrincipal Long userId
    ) {
        var response = DatasetDownloadUrlResponse.from(
                issueDatasetDownloadUrlUseCase.issueDownloadUrl(
                        userId,
                        problemSetId
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "데이터셋 다운로드 URL이 발급되었습니다.",
                response
        ));
    }
}
