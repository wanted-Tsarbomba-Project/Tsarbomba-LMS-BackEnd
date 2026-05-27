package com.wanted.codebombalms.problems.set.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.set.application.command.DeleteProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.command.ProblemCreateCommand;
import com.wanted.codebombalms.problems.set.application.command.ProblemUpdateCommand;
import com.wanted.codebombalms.problems.set.application.command.RegisterProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.command.UpdateProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.usecase.DeleteProblemSetUseCase;
import com.wanted.codebombalms.problems.set.application.usecase.RegisterProblemSetUseCase;
import com.wanted.codebombalms.problems.set.application.usecase.UpdateProblemSetUseCase;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemCreateRequest;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemSetCreateRequest;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemSetUpdateRequest;
import com.wanted.codebombalms.problems.set.presentation.request.ProblemUpdateRequest;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetCreateResponse;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetDeleteResponse;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "문제 관리", description = "운영자가 문제 세트와 소문제를 등록, 수정, 삭제하는 API")
@RestController
@RequiredArgsConstructor
public class ProblemManageController {

    private final RegisterProblemSetUseCase registerProblemSetUseCase;
    private final UpdateProblemSetUseCase updateProblemSetUseCase;
    private final DeleteProblemSetUseCase deleteProblemSetUseCase;

    @Operation(
            summary = "문제 세트 등록",
            description = "운영자가 문제 세트와 소문제 목록을 함께 등록합니다. "
                    + "difficulty는 EASY, MEDIUM, HARD 값을 사용합니다. "
                    + "point는 정답 시 지급할 포인트이며, score와는 분리된 값입니다. "
                    + "dataFileName은 현재 파일명 참고용으로 사용되며, 실제 파일은 데이터셋 업로드 API로 업로드합니다. "
                    + "코드 실행형 문제의 실제 CSV 파일은 데이터셋 업로드 후 연결 API로 연결합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "문제 세트 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 세트 등록 성공",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 201,
                                              "code": "COMMON-CREATED",
                                              "message": "요청이 성공적으로 생성되었습니다.",
                                              "data": {
                                                "problemSetId": 3001,
                                                "title": "pandas 기초 분석 문제 세트",
                                                "categoryName": "Python 데이터 분석",
                                                "totalProblemCount": 2,
                                                "createdProblemCount": 2
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-SET-006, PRB-CAT-003, PRB-PBL-009, PRB-PBL-006 등 입력값 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "문제 세트 제목 누락",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 400,
                                                      "code": "PRB-SET-006",
                                                      "message": "문제 세트 제목은 필수입니다.",
                                                      "path": "/api/v1/problems"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "소문제 목록 누락",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 400,
                                                      "code": "PRB-PBL-009",
                                                      "message": "소문제는 1개 이상 필요합니다.",
                                                      "path": "/api/v1/problems"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "소문제 포인트 오류",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 400,
                                                      "code": "PRB-PBL-010",
                                                      "message": "문제 포인트는 1 이상이어야 합니다.",
                                                      "path": "/api/v1/problems"
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
                                              "path": "/api/v1/problems"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-CAT-001 - 문제 분야를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "카테고리 없음",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 404,
                                              "code": "PRB-CAT-001",
                                              "message": "문제 분야를 찾을 수 없습니다.",
                                              "path": "/api/v1/problems"
                                            }
                                            """
                            )
                    )
            )
    })
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
            description = "문제 세트 기본 정보와 소문제 목록을 수정합니다. "
                    + "소문제 수정 시 problemId가 있는 항목은 기존 문제를 수정하고, 구현 정책에 따라 새 문제를 추가할 수 있습니다. "
                    + "problemId가 있는 소문제는 기존 소문제 수정 대상으로 해석됩니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문제 세트 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 세트 수정 성공",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 200,
                                              "code": "COMMON-SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "problemSetId": 3001,
                                                "title": "pandas 기초 분석 문제 세트",
                                                "categoryName": "Python 데이터 분석",
                                                "updatedProblemCount": 2
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-SET-006, PRB-CAT-003, PRB-PBL-006 등 입력값 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "소문제 제목 누락",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 400,
                                              "code": "PRB-PBL-006",
                                              "message": "소문제 제목은 필수입니다.",
                                              "path": "/api/v1/problems/3001"
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
                                              "path": "/api/v1/problems/3001"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-SET-001 - 문제 세트를 찾을 수 없음 또는 PRB-PBL-001 - 소문제를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "문제 세트 없음",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 404,
                                                      "code": "PRB-SET-001",
                                                      "message": "문제 세트를 찾을 수 없습니다.",
                                                      "path": "/api/v1/problems/9999"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "소문제 없음",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 404,
                                                      "code": "PRB-PBL-001",
                                                      "message": "문제를 찾을 수 없습니다.",
                                                      "path": "/api/v1/problems/3001"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
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
            description = "문제 세트를 삭제하거나 비활성화합니다. "
                    + "force가 false이면 제출 기록이 있는 문제 세트는 삭제하지 않습니다. "
                    + "force가 true이면 정책에 따라 강제 삭제 또는 비활성화를 시도합니다. "
                    + "삭제 결과로 변경된 문제 세트 상태와 비활성화된 소문제 수를 반환합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문제 세트 삭제 또는 비활성화 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 세트 비활성화 성공",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 200,
                                              "code": "COMMON-SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "problemSetId": 3001,
                                                "status": "INACTIVE",
                                                "deactivatedProblemCount": 2
                                              }
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
                                              "path": "/api/v1/problems/3001"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-SET-001 - 문제 세트를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 세트 없음",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 404,
                                              "code": "PRB-SET-001",
                                              "message": "문제 세트를 찾을 수 없습니다.",
                                              "path": "/api/v1/problems/9999"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "PRB-PBL-004 - 제출 기록이 존재해 삭제 불가",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "제출 기록 존재",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 409,
                                              "code": "PRB-PBL-004",
                                              "message": "제출 기록이 존재합니다.",
                                              "path": "/api/v1/problems/3001"
                                            }
                                            """
                            )
                    )
            )
    })
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
