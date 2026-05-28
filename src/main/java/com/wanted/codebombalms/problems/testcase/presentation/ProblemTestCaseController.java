package com.wanted.codebombalms.problems.testcase.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.testcase.application.command.CreateProblemTestCaseCommand;
import com.wanted.codebombalms.problems.testcase.application.command.UpdateProblemTestCaseCommand;
import com.wanted.codebombalms.problems.testcase.application.query.GetProblemTestCasesQuery;
import com.wanted.codebombalms.problems.testcase.application.usecase.ProblemTestCaseCommandUseCase;
import com.wanted.codebombalms.problems.testcase.application.usecase.ProblemTestCaseQueryUseCase;
import com.wanted.codebombalms.problems.testcase.presentation.request.CreateProblemTestCaseRequest;
import com.wanted.codebombalms.problems.testcase.presentation.request.UpdateProblemTestCaseRequest;
import com.wanted.codebombalms.problems.testcase.presentation.response.ProblemTestCaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "테스트케이스", description = "코드 문제 채점에 사용되는 테스트케이스 관리 API")
@RestController
public class ProblemTestCaseController {

    private final ProblemTestCaseCommandUseCase commandUseCase;
    private final ProblemTestCaseQueryUseCase queryUseCase;

    public ProblemTestCaseController(
            ProblemTestCaseCommandUseCase commandUseCase,
            ProblemTestCaseQueryUseCase queryUseCase
    ) {
        this.commandUseCase = commandUseCase;
        this.queryUseCase = queryUseCase;
    }

    @Operation(
            summary = "테스트케이스 등록",
            description = "운영자가 코드 문제의 채점 기준이 되는 테스트케이스를 등록합니다. "
                    + "testCode는 사용자 코드 실행 후 함께 실행되는 검증 코드입니다. "
                    + "isHidden이 true이면 학생에게 상세 결과를 노출하지 않습니다. "
                    + "성공 시 201을 반환합니다. 에러: PRB-PBL-001 문제 없음, PRB-TC-001 테스트케이스 입력값 오류"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "테스트케이스 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "테스트케이스 등록 성공",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 201,
                                          "code": "COMMON-CREATED",
                                          "message": "요청이 성공적으로 생성되었습니다.",
                                          "data": {
                                            "testCaseId": 3001,
                                            "problemId": 3001,
                                            "testCode": "assert result == df.shape",
                                            "expectedResult": "df.shape",
                                            "testOrder": 1,
                                            "isHidden": false,
                                            "timeoutMs": 3000,
                                            "status": "ACTIVE"
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-TC-002 - 테스트케이스 입력값 오류 또는 PRB-TC-004 - 코드 문제에만 등록 가능",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "입력값 오류",
                                            value = """
                                                {
                                                  "timestamp": "2026-05-27T12:00:00",
                                                  "status": 400,
                                                  "code": "PRB-TC-002",
                                                  "message": "테스트케이스 입력값이 올바르지 않습니다.",
                                                  "path": "/api/v1/problems/3001/test-cases"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "코드 문제 아님",
                                            value = """
                                                {
                                                  "timestamp": "2026-05-27T12:00:00",
                                                  "status": 400,
                                                  "code": "PRB-TC-004",
                                                  "message": "코드 문제에만 테스트케이스를 등록할 수 있습니다.",
                                                  "path": "/api/v1/problems/3001/test-cases"
                                                }
                                                """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-PBL-001 - 문제를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 없음",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 404,
                                          "code": "PRB-PBL-001",
                                          "message": "문제를 찾을 수 없습니다.",
                                          "path": "/api/v1/problems/9999/test-cases"
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "PRB-TC-003 - 이미 등록된 테스트케이스",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "이미 등록됨",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 409,
                                          "code": "PRB-TC-003",
                                          "message": "이미 등록된 테스트케이스가 있습니다.",
                                          "path": "/api/v1/problems/3001/test-cases"
                                        }
                                        """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping("/api/v1/problems/{problemId}/test-cases")
    public ResponseEntity<ApiResponse<ProblemTestCaseResponse>> createTestCase(
            @Parameter(description = "테스트케이스를 등록할 코드 문제 ID", example = "3001")
            @PathVariable Long problemId,
            @RequestBody CreateProblemTestCaseRequest request
    ) {
        var view = commandUseCase.handle(new CreateProblemTestCaseCommand(
                problemId,
                request.testCode(),
                request.expectedResult(),
                request.testOrder(),
                request.isHidden(),
                request.timeoutMs()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        ApiResponseMessage.CREATED,
                        ProblemTestCaseResponse.from(view)
                ));
    }

    @Operation(
            summary = "테스트케이스 목록 조회",
            description = "특정 코드 문제에 등록된 테스트케이스 목록을 조회합니다. "
                    + "운영자용 API이므로 히든 테스트케이스의 testCode와 expectedResult도 함께 반환됩니다. "
                    + "성공 시 200을 반환합니다. 에러: PRB-PBL-001 문제 없음"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "테스트케이스 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "테스트케이스 목록",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 200,
                                          "code": "COMMON-SUCCESS",
                                          "message": "요청이 성공적으로 처리되었습니다.",
                                          "data": [
                                            {
                                              "testCaseId": 3001,
                                              "problemId": 3001,
                                              "testCode": "assert result == df.shape",
                                              "expectedResult": "df.shape",
                                              "testOrder": 1,
                                              "isHidden": false,
                                              "timeoutMs": 3000,
                                              "status": "ACTIVE"
                                            },
                                            {
                                              "testCaseId": 3002,
                                              "problemId": 3001,
                                              "testCode": "assert result[0] > 0",
                                              "expectedResult": "행 개수는 0보다 커야 함",
                                              "testOrder": 2,
                                              "isHidden": true,
                                              "timeoutMs": 3000,
                                              "status": "ACTIVE"
                                            }
                                          ]
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-PBL-001 - 문제를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 없음",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 404,
                                          "code": "PRB-PBL-001",
                                          "message": "문제를 찾을 수 없습니다.",
                                          "path": "/api/v1/problems/9999/test-cases"
                                        }
                                        """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping("/api/v1/problems/{problemId}/test-cases")
    public ResponseEntity<ApiResponse<List<ProblemTestCaseResponse>>> findTestCases(
            @Parameter(description = "테스트케이스 목록을 조회할 코드 문제 ID", example = "3001")
            @PathVariable Long problemId
    ) {
        List<ProblemTestCaseResponse> responses = queryUseCase.handle(new GetProblemTestCasesQuery(problemId))
                .stream()
                .map(ProblemTestCaseResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                responses
        ));
    }

    @Operation(
            summary = "테스트케이스 수정",
            description = "테스트케이스의 검증 코드, 기대 결과, 순서, 히든 여부, 타임아웃을 수정합니다. "
                    + "성공 시 200을 반환합니다. 에러: PRB-TC-001 테스트케이스 입력값 오류, PRB-TC-002 테스트케이스 없음"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "테스트케이스 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "테스트케이스 수정 성공",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 200,
                                          "code": "COMMON-SUCCESS",
                                          "message": "요청이 성공적으로 처리되었습니다.",
                                          "data": {
                                            "testCaseId": 3001,
                                            "problemId": 3001,
                                            "testCode": "assert result == df.shape",
                                            "expectedResult": "df.shape",
                                            "testOrder": 1,
                                            "isHidden": true,
                                            "timeoutMs": 3000,
                                            "status": "ACTIVE"
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-TC-002 - 테스트케이스 입력값 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "입력값 오류",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 400,
                                          "code": "PRB-TC-002",
                                          "message": "테스트케이스 입력값이 올바르지 않습니다.",
                                          "path": "/api/v1/test-cases/3001"
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-TC-001 - 테스트케이스를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "테스트케이스 없음",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 404,
                                          "code": "PRB-TC-001",
                                          "message": "테스트케이스를 찾을 수 없습니다.",
                                          "path": "/api/v1/test-cases/9999"
                                        }
                                        """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PutMapping("/api/v1/test-cases/{testCaseId}")
    public ResponseEntity<ApiResponse<ProblemTestCaseResponse>> updateTestCase(
            @Parameter(description = "수정할 테스트케이스 ID", example = "3001")
            @PathVariable Long testCaseId,
            @RequestBody UpdateProblemTestCaseRequest request
    ) {
        var view = commandUseCase.handle(new UpdateProblemTestCaseCommand(
                testCaseId,
                request.testCode(),
                request.expectedResult(),
                request.testOrder(),
                request.isHidden(),
                request.timeoutMs()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                ProblemTestCaseResponse.from(view)
        ));
    }

    @Operation(
            summary = "테스트케이스 삭제",
            description = "테스트케이스를 삭제하거나 비활성화합니다. "
                    + "성공 시 200을 반환합니다. 에러: PRB-TC-002 테스트케이스 없음"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "테스트케이스 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "테스트케이스 삭제 성공",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 200,
                                          "code": "COMMON-SUCCESS",
                                          "message": "요청이 성공적으로 처리되었습니다.",
                                          "data": {
                                            "testCaseId": 3001,
                                            "problemId": 3001,
                                            "testCode": "assert result == df.shape",
                                            "expectedResult": "df.shape",
                                            "testOrder": 1,
                                            "isHidden": false,
                                            "timeoutMs": 3000,
                                            "status": "INACTIVE"
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-TC-001 - 테스트케이스를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "테스트케이스 없음",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 404,
                                          "code": "PRB-TC-001",
                                          "message": "테스트케이스를 찾을 수 없습니다.",
                                          "path": "/api/v1/test-cases/9999"
                                        }
                                        """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @DeleteMapping("/api/v1/test-cases/{testCaseId}")
    public ResponseEntity<ApiResponse<ProblemTestCaseResponse>> deleteTestCase(
            @Parameter(description = "삭제할 테스트케이스 ID", example = "3001")
            @PathVariable Long testCaseId
    ) {
        var view = commandUseCase.delete(testCaseId);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                ProblemTestCaseResponse.from(view)
        ));
    }
}
