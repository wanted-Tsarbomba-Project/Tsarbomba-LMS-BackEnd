package com.wanted.codebombalms.problems.dataset.presentation;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.dataset.application.command.ConnectProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.usecase.ConnectProblemDatasetUseCase;
import com.wanted.codebombalms.problems.dataset.application.usecase.UploadProblemDatasetUseCase;
import com.wanted.codebombalms.problems.dataset.presentation.request.ProblemDatasetConnectRequest;
import com.wanted.codebombalms.problems.dataset.presentation.response.ProblemDatasetConnectResponse;
import com.wanted.codebombalms.problems.dataset.presentation.response.ProblemDatasetUploadResponse;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "문제 데이터셋", description = "코드 실행형 문제에서 사용할 데이터셋 업로드 및 연결 API")
@RestController
@RequiredArgsConstructor
public class ProblemDatasetController {

    private final ConnectProblemDatasetUseCase connectProblemDatasetUseCase;
    private final UploadProblemDatasetUseCase uploadProblemDatasetUseCase;

    @Operation(
            summary = "문제-데이터셋 연결",
            description = "이미 업로드된 데이터셋을 코드 문제에 연결합니다. "
                    + "연결이 성공하면 시작 코드에 pandas read_csv 경로를 포함해 반환합니다. "
                    + "startCode는 GCS URL을 pandas로 읽을 수 있는 형태입니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "문제-데이터셋 연결 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "데이터셋 연결 성공",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 201,
                                              "code": "COMMON-CREATED",
                                              "message": "요청이 성공적으로 생성되었습니다.",
                                              "data": {
                                                "problemId": 3001,
                                                "datasetId": 3001,
                                                "startCode": "import pandas as pd\\n\\ndf = pd.read_csv(\\"https://storage.googleapis.com/codebombalms/problem_dataset/28679fde-6075-4c2e-a3f0-190fa3d80db7_employee_performance.csv\\")"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-DAT-003 - 코드 실행형 문제가 아니어서 데이터셋 연결 불가",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "연결할 수 없는 문제 유형",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 400,
                                              "code": "PRB-DAT-003",
                                              "message": "코드 실행형 문제에만 데이터셋을 연결할 수 있습니다.",
                                              "path": "/api/v1/problems/3001/datasets"
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
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 401,
                                              "code": "AUT-003",
                                              "message": "만료된 토큰입니다.",
                                              "path": "/api/v1/problems/3001/datasets"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-PBL-001 - 문제를 찾을 수 없음 또는 PRB-DAT-001 - 데이터셋을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "문제 없음",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 404,
                                                      "code": "PRB-PBL-001",
                                                      "message": "문제를 찾을 수 없습니다.",
                                                      "path": "/api/v1/problems/9999/datasets"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "데이터셋 없음",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 404,
                                                      "code": "PRB-DAT-001",
                                                      "message": "데이터셋을 찾을 수 없습니다.",
                                                      "path": "/api/v1/problems/3001/datasets"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "PRB-DAT-002 - 이미 연결된 데이터셋",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "이미 연결된 데이터셋",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 409,
                                              "code": "PRB-DAT-002",
                                              "message": "이미 문제에 연결된 데이터셋입니다.",
                                              "path": "/api/v1/problems/3001/datasets"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "PRB-002 - 문제 도메인 서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "서버 오류",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 500,
                                              "code": "PRB-002",
                                              "message": "문제 도메인 처리 중 서버 오류가 발생했습니다.",
                                              "path": "/api/v1/problems/3001/datasets"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/api/v1/problem-sets/{problemSetId}/datasets")
    public ResponseEntity<ApiResponse<ProblemDatasetConnectResponse>> connectDataset(
            @Parameter(description = "데이터셋을 연결할 코드 문제 ID", example = "3001")
            @PathVariable Long problemSetId,
            @Valid @RequestBody ProblemDatasetConnectRequest request
    ) {
        var command = new ConnectProblemDatasetCommand(problemSetId, request.datasetId());
        var response = new ProblemDatasetConnectResponse(
                connectProblemDatasetUseCase.handle(command)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        ApiResponseMessage.CREATED,
                        response
                ));
    }

    @Operation(
            summary = "데이터셋 업로드",
            description = "CSV 등 문제 풀이에 필요한 데이터셋 파일을 GCS에 업로드합니다. "
                    + "요청은 multipart/form-data이며 파일 파트 이름은 datasetFile입니다. "
                    + "저장 파일명은 UUID를 붙여 생성하며, fileUrl은 클라우드 접근용 URL입니다. "
                    + "GCS 버킷은 codebombalms, 객체 경로 prefix는 problem_dataset/ 입니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "데이터셋 업로드 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "CSV 데이터셋 업로드 성공",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 201,
                                              "code": "COMMON-CREATED",
                                              "message": "요청이 성공적으로 생성되었습니다.",
                                              "data": {
                                                "datasetId": 3001,
                                                "originalFileName": "employee_performance.csv",
                                                "storedFileName": "28679fde-6075-4c2e-a3f0-190fa3d80db7_employee_performance.csv",
                                                "fileUrl": "https://storage.googleapis.com/codebombalms/problem_dataset/28679fde-6075-4c2e-a3f0-190fa3d80db7_employee_performance.csv",
                                                "filePath": "problem_dataset/28679fde-6075-4c2e-a3f0-190fa3d80db7_employee_performance.csv",
                                                "status": "ACTIVE"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-DAT-004 - CSV 파일만 업로드 가능 또는 PRB-DAT-005 - 데이터셋 업로드 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "CSV 파일 아님",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 400,
                                                      "code": "PRB-DAT-004",
                                                      "message": "CSV 파일만 업로드할 수 있습니다.",
                                                      "path": "/api/v1/problem-datasets"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "업로드 실패",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 400,
                                                      "code": "PRB-DAT-005",
                                                      "message": "데이터셋 업로드에 실패했습니다.",
                                                      "path": "/api/v1/problem-datasets"
                                                    }
                                                    """
                                    )
                            }
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
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 401,
                                              "code": "AUT-003",
                                              "message": "만료된 토큰입니다.",
                                              "path": "/api/v1/problem-datasets"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "PRB-002 - 문제 도메인 서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "서버 오류",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 500,
                                              "code": "PRB-002",
                                              "message": "문제 도메인 처리 중 서버 오류가 발생했습니다.",
                                              "path": "/api/v1/problem-datasets"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping(
            value = "/api/v1/problem-datasets",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ProblemDatasetUploadResponse>> uploadDataset(
            @Parameter(description = "업로드할 CSV 데이터셋 파일. multipart/form-data의 datasetFile 파트로 전달합니다.", required = true)
            @RequestPart("datasetFile") MultipartFile datasetFile
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
                    datasetFile.getBytes(),
                    datasetFile.getSize()
            );
        } catch (IOException e) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_UPLOAD_FAILED);
        }
    }
}
