package com.wanted.codebombalms.problems.set.presentation;

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
import com.wanted.codebombalms.problems.set.application.query.GetProblemSetForUpdateQuery;
import com.wanted.codebombalms.problems.set.application.usecase.*;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemCreateRequest;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemSetCreateRequest;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemSetUpdateRequest;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemSetWithDatasetCreateSwaggerRequest;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemUpdateRequest;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetCreateResponse;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetDeleteResponse;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetForUpdateResponse;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetUpdateResponse;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetWithDatasetCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "문제 관리", description = "운영자가 문제 세트와 소문제를 등록, 수정, 삭제하는 API")
@RestController
@RequiredArgsConstructor
public class ProblemManageController {

    private final GetProblemSetForUpdateUseCase getProblemSetForUpdateUseCase;
    private final RegisterProblemSetUseCase registerProblemSetUseCase;
    private final RegisterProblemSetWithDatasetUseCase registerProblemSetWithDatasetUseCase;
    private final UpdateProblemSetUseCase updateProblemSetUseCase;
    private final DeleteProblemSetUseCase deleteProblemSetUseCase;
    private final UpdateProblemSetWithDatasetUseCase updateProblemSetWithDatasetUseCase;
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "문제 세트 등록",
            description = "데이터셋 파일 없이 문제 세트와 소문제 목록을 등록합니다."
    )
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
            description = "문제 세트 기본 정보와 소문제 목록을 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문제 세트 및 데이터셋 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "status": 200,
                                  "code": "COMMON-SUCCESS",
                                  "message": "요청이 성공적으로 처리되었습니다.",
                                  "data": {
                                    "problemSetId": 3001,
                                    "title": "pandas 기초 분석 문제 세트",
                                    "categoryName": "Python 데이터 분석",
                                    "totalProblemCount": 2
                                  }
                                }
                                """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-SET-006, PRB-PBL-009, PRB-DAT-004, PRB-DAT-005 - 입력값 오류 또는 CSV 업로드 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "AUT-003, AUT-016 - 인증 토큰 만료 또는 인증 필요"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "AUT-015 - 접근 권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-SET-001 - 문제 세트를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "PRB-002 - 문제 도메인 처리 중 서버 오류"
            )
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
            description = "문제 세트 등록 JSON 파트와 CSV 데이터셋 파일 파트를 multipart/form-data로 함께 받아 등록합니다."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = ProblemSetWithDatasetCreateSwaggerRequest.class)
            )
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping(
            value = "/api/v1/problems/with-dataset",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ProblemSetWithDatasetCreateResponse>> createProblemWithDataset(
            @Parameter(description = "문제 세트 등록 JSON 파트", required = true)
            @RequestPart("request") ProblemSetCreateRequest request,
            @Parameter(description = "문제 세트에 연결할 CSV 데이터셋 파일", required = true)
            @RequestPart("datasetFile") MultipartFile datasetFile
    ) {
        var result = registerProblemSetWithDatasetUseCase.handle(
                toCommand(request),
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

    @Operation(
            summary = "문제 세트 수정을 위한 상세 조회",
            description = "관리자가 문제 세트 수정 화면에 진입했을 때 기존 문제 세트, 데이터셋, 소문제 정보를 조회합니다."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping("/api/v1/problems/{problemSetId}")
    public ResponseEntity<ApiResponse<ProblemSetForUpdateResponse>> findProblemSetForUpdate(
            @Parameter(description = "수정할 문제 세트 ID", example = "3001")
            @PathVariable Long problemSetId
    ) {
        var result = getProblemSetForUpdateUseCase.handle(
                new GetProblemSetForUpdateQuery(problemSetId)
        );

        var response = new ProblemSetForUpdateResponse(result);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PutMapping(
            value = "/api/v1/problems/{problemSetId}/with-dataset",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ProblemSetUpdateResponse>> updateProblemSetWithDataset(
            @PathVariable Long problemSetId,
            @RequestPart("request") String requestJson,
            @RequestPart("datasetFile") MultipartFile datasetFile
    ) {
        ProblemSetUpdateRequest request = parseProblemSetUpdateRequest(requestJson);

        var result = updateProblemSetWithDatasetUseCase.handle(
                toCommand(problemSetId, request),
                toDatasetCommand(datasetFile)
        );

        var response = new ProblemSetUpdateResponse(result);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    private ProblemSetUpdateRequest parseProblemSetUpdateRequest(String requestJson) {
        try {
            return objectMapper.readValue(requestJson, ProblemSetUpdateRequest.class);
        } catch (IOException e) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_INVALID_INPUT);
        }
    }

    private UploadProblemDatasetCommand toDatasetCommand(MultipartFile datasetFile) {
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
