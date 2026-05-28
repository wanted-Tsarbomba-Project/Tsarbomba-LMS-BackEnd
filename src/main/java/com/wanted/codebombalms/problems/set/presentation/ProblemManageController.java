package com.wanted.codebombalms.problems.set.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.command.DeleteProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.command.ProblemCreateCommand;
import com.wanted.codebombalms.problems.set.application.command.ProblemUpdateCommand;
import com.wanted.codebombalms.problems.set.application.command.RegisterProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.command.UpdateProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.usecase.DeleteProblemSetUseCase;
import com.wanted.codebombalms.problems.set.application.usecase.RegisterProblemSetUseCase;
import com.wanted.codebombalms.problems.set.application.usecase.RegisterProblemSetWithDatasetUseCase;
import com.wanted.codebombalms.problems.set.application.usecase.UpdateProblemSetUseCase;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemCreateRequest;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemSetCreateRequest;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemSetUpdateRequest;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemSetWithDatasetCreateSwaggerRequest;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemUpdateRequest;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetCreateResponse;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetDeleteResponse;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetUpdateResponse;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetWithDatasetCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "문제 관리", description = "운영자가 문제 세트와 소문제를 등록, 수정, 삭제하는 API")
@RestController
@RequiredArgsConstructor
public class ProblemManageController {

    private final ObjectMapper objectMapper;
    private final RegisterProblemSetUseCase registerProblemSetUseCase;
    private final RegisterProblemSetWithDatasetUseCase registerProblemSetWithDatasetUseCase;
    private final UpdateProblemSetUseCase updateProblemSetUseCase;
    private final DeleteProblemSetUseCase deleteProblemSetUseCase;

    @Operation(
            summary = "문제 세트 등록",
            description = "운영자가 문제 세트와 소문제 목록을 함께 등록합니다. 데이터셋 파일까지 함께 등록하려면 /api/v1/problems/with-dataset API를 사용합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "문제 세트 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-SET-006, PRB-CAT-003, PRB-PBL-009, PRB-PBL-010 - 입력값 오류",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "timestamp": "2026-05-28T12:00:00",
                              "status": 400,
                              "code": "PRB-PBL-009",
                              "message": "소문제는 1개 이상 필요합니다.",
                              "path": "/api/v1/problems"
                            }
                            """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-003 - 인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "PRB-CAT-001 - 문제 카테고리를 찾을 수 없음")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping("/api/v1/problems")
    public ResponseEntity<ApiResponse<ProblemSetCreateResponse>> createProblem(
            @RequestBody ProblemSetCreateRequest request
    ) {
        var result = registerProblemSetUseCase.handle(toCommand(request));
        var response = new ProblemSetCreateResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        ApiResponseMessage.CREATED,
                        response
                ));
    }

    @Operation(
            summary = "문제 세트 수정",
            description = "문제 세트 기본 정보와 소문제 목록을 수정합니다. problemId가 있는 소문제는 기존 문제를 수정합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "문제 세트 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "PRB-SET-006, PRB-CAT-003, PRB-PBL-006 - 입력값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-003 - 인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "PRB-SET-001 또는 PRB-PBL-001 - 문제 세트 또는 소문제를 찾을 수 없음")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PutMapping("/api/v1/problems/{problemSetId}")
    public ResponseEntity<ApiResponse<ProblemSetUpdateResponse>> updateProblemSet(
            @Parameter(description = "수정할 문제 세트 ID", example = "3001")
            @PathVariable Long problemSetId,
            @RequestBody ProblemSetUpdateRequest request
    ) {
        var result = updateProblemSetUseCase.handle(toCommand(problemSetId, request));
        var response = new ProblemSetUpdateResponse(result);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    @Operation(
            summary = "문제 세트 삭제",
            description = "문제 세트를 삭제하거나 비활성화합니다. force=false이면 제출 기록이 있는 문제 세트는 삭제하지 않습니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "문제 세트 삭제 또는 비활성화 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-003 - 인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "PRB-SET-001 - 문제 세트를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "PRB-PBL-004 - 제출 기록이 존재하여 삭제 불가")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @DeleteMapping("/api/v1/problems/{problemSetId}")
    public ResponseEntity<ApiResponse<ProblemSetDeleteResponse>> deleteProblemSet(
            @Parameter(description = "삭제할 문제 세트 ID", example = "3001")
            @PathVariable Long problemSetId,
            @Parameter(description = "제출 기록이 있어도 강제 처리할지 여부", example = "false")
            @RequestParam(defaultValue = "false") boolean force
    ) {
        var result = deleteProblemSetUseCase.handle(new DeleteProblemSetCommand(problemSetId, force));
        var response = new ProblemSetDeleteResponse(result);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    @Operation(
            summary = "데이터셋 포함 문제 세트 등록",
            description = "문제 세트 정보와 CSV 데이터셋 파일을 multipart/form-data로 함께 받아 등록합니다. 문제 세트와 소문제를 생성한 뒤 CSV를 GCS에 업로드하고, 업로드된 데이터셋을 생성된 문제 세트에 연결합니다. 연결된 데이터셋 URL은 해당 문제 세트 안의 코드 실행형 소문제 시작 코드로 제공됩니다."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = ProblemSetWithDatasetCreateSwaggerRequest.class),
                    examples = @ExampleObject(
                            name = "문제 세트와 CSV 파일 등록 요청",
                            value = """
                                    {
                                      "request": {
                                        "title": "pandas 기초 분석 문제 세트",
                                        "categoryName": "Python 데이터 분석",
                                        "description": "CSV 데이터를 활용한 코드 실행형 문제 세트입니다.",
                                        "difficulty": "EASY",
                                        "dataFileName": "employee_performance.csv",
                                        "problems": [
                                          {
                                            "title": "데이터 행과 열 개수 확인",
                                            "content": "DataFrame의 행과 열 개수를 확인하세요.",
                                            "point": 10,
                                            "startCode": null,
                                            "answer": null,
                                            "hint": "shape 속성을 사용해보세요.",
                                            "explanation": "df.shape를 사용하면 행과 열 개수를 확인할 수 있습니다."
                                          }
                                        ]
                                      },
                                      "datasetFile": "employee_performance.csv"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "데이터셋 포함 문제 세트 등록 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "timestamp": "2026-05-28T12:00:00",
                              "status": 201,
                              "code": "COMMON-CREATED",
                              "message": "리소스가 성공적으로 생성되었습니다.",
                              "data": {
                                "problemSetId": 3001,
                                "datasetId": 5001,
                                "title": "pandas 기초 분석 문제 세트",
                                "categoryName": "Python 데이터 분석",
                                "totalProblemCount": 1,
                                "createdProblemCount": 1,
                                "datasetFileName": "employee_performance.csv",
                                "datasetUrl": "https://storage.googleapis.com/codebombalms/problem_dataset/uuid_employee_performance.csv",
                                "startCode": "import pandas as pd\\n\\ndf = pd.read_csv('https://storage.googleapis.com/codebombalms/problem_dataset/uuid_employee_performance.csv')"
                              }
                            }
                            """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-SET-006, PRB-PBL-009, PRB-DAT-004, PRB-DAT-005 - 입력값 오류 또는 CSV 업로드 실패",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "CSV 파일 오류", value = """
                                    {
                                      "timestamp": "2026-05-28T12:00:00",
                                      "status": 400,
                                      "code": "PRB-DAT-004",
                                      "message": "CSV 파일만 업로드할 수 있습니다.",
                                      "path": "/api/v1/problems/with-dataset"
                                    }
                                    """),
                            @ExampleObject(name = "소문제 목록 누락", value = """
                                    {
                                      "timestamp": "2026-05-28T12:00:00",
                                      "status": 400,
                                      "code": "PRB-PBL-009",
                                      "message": "소문제는 1개 이상 필요합니다.",
                                      "path": "/api/v1/problems/with-dataset"
                                    }
                                    """)
                    })
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
                                  "timestamp": "2026-05-28T12:00:00Z",
                                  "status": 401,
                                  "code": "AUT-003",
                                  "message": "만료된 토큰입니다.",
                                  "path": "/api/v1/problems/with-dataset"
                                }
                                """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-CAT-001 - 문제 카테고리를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "카테고리 없음",
                                    value = """
                                {
                                  "timestamp": "2026-05-28T12:00:00Z",
                                  "status": 404,
                                  "code": "PRB-CAT-001",
                                  "message": "문제 분야를 찾을 수 없습니다.",
                                  "path": "/api/v1/problems/with-dataset"
                                }
                                """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "PRB-002 - 문제 도메인 처리 중 서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "서버 오류",
                                    value = """
                                {
                                  "timestamp": "2026-05-28T12:00:00Z",
                                  "status": 500,
                                  "code": "PRB-002",
                                  "message": "문제 도메인 처리 중 서버 오류가 발생했습니다.",
                                  "path": "/api/v1/problems/with-dataset"
                                }
                                """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping(
            value = "/api/v1/problems/with-dataset",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ProblemSetWithDatasetCreateResponse>> createProblemWithDataset(
            @Parameter(description = "문제 세트 등록 JSON 파트", required = true)
            @RequestParam("request") String request,
            @Parameter(description = "문제 세트에 연결할 CSV 데이터셋 파일", required = true)
            @RequestParam("datasetFile") MultipartFile datasetFile
    ) {
        var result = registerProblemSetWithDatasetUseCase.handle(
                toCommand(parseProblemSetCreateRequest(request)),
                toDatasetCommand(datasetFile)
        );

        var response = new ProblemSetWithDatasetCreateResponse(
                result.problemSetId(),
                result.datasetId(),
                result.title(),
                result.categoryName(),
                result.totalProblemCount(),
                result.createdProblemCount(),
                result.datasetFileName(),
                result.datasetUrl(),
                result.startCode()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        ApiResponseMessage.CREATED,
                        response
                ));
    }


    private ProblemSetCreateRequest parseProblemSetCreateRequest(String request) {
        try {
            return objectMapper.readValue(request, ProblemSetCreateRequest.class);
        } catch (JsonProcessingException e) {
            throw new ValidationException(ProblemErrorCode.INVALID_INPUT);
        }
    }
    private UploadProblemDatasetCommand toDatasetCommand(MultipartFile datasetFile) {
        try {
            return new UploadProblemDatasetCommand(
                    datasetFile.getOriginalFilename(),
                    datasetFile.getBytes(),
                    datasetFile.getSize()
            );
        } catch (IOException e) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_UPLOAD_FAILED);
        }
    }

    private RegisterProblemSetCommand toCommand(ProblemSetCreateRequest request) {
        List<ProblemCreateCommand> problems = request.problems() == null
                ? null
                : request.problems()
                        .stream()
                        .map(this::toCommand)
                        .toList();

        return new RegisterProblemSetCommand(
                request.title(),
                request.categoryName(),
                request.difficulty(),
                request.description(),
                request.dataFileName(),
                problems
        );
    }

    private ProblemCreateCommand toCommand(ProblemCreateRequest request) {
        return new ProblemCreateCommand(
                request.title(),
                request.content(),
                request.point(),
                request.startCode(),
                request.answer(),
                request.hint(),
                request.explanation()
        );
    }

    private UpdateProblemSetCommand toCommand(Long problemSetId, ProblemSetUpdateRequest request) {
        List<ProblemUpdateCommand> problems = request.problems() == null
                ? null
                : request.problems()
                        .stream()
                        .map(this::toCommand)
                        .toList();

        return new UpdateProblemSetCommand(
                problemSetId,
                request.title(),
                request.categoryName(),
                request.difficulty(),
                request.description(),
                request.dataFileName(),
                problems
        );
    }

    private ProblemUpdateCommand toCommand(ProblemUpdateRequest request) {
        return new ProblemUpdateCommand(
                request.problemId(),
                request.title(),
                request.content(),
                request.point(),
                request.startCode(),
                request.answer(),
                request.hintId(),
                request.hint(),
                request.explanation()
        );
    }
}
