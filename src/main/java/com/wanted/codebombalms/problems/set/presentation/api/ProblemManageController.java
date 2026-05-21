package com.wanted.codebombalms.problems.set.presentation.api;

import com.wanted.codebombalms.problems.set.application.command.DeleteProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.command.ProblemCreateCommand;
import com.wanted.codebombalms.problems.set.application.command.ProblemUpdateCommand;
import com.wanted.codebombalms.problems.set.application.command.RegisterProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.command.UpdateProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.usecase.DeleteProblemSetUseCase;
import com.wanted.codebombalms.problems.set.application.usecase.RegisterProblemSetUseCase;
import com.wanted.codebombalms.problems.set.application.usecase.UpdateProblemSetUseCase;
import com.wanted.codebombalms.problems.set.presentation.api.request.ProblemCreateRequest;
import com.wanted.codebombalms.problems.set.presentation.api.request.ProblemSetCreateRequest;
import com.wanted.codebombalms.problems.set.presentation.api.request.ProblemSetUpdateRequest;
import com.wanted.codebombalms.problems.set.presentation.api.request.ProblemUpdateRequest;
import com.wanted.codebombalms.problems.set.presentation.api.response.ProblemSetCreateResponse;
import com.wanted.codebombalms.problems.set.presentation.api.response.ProblemSetDeleteResponse;
import com.wanted.codebombalms.problems.set.presentation.api.response.ProblemSetUpdateResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
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

@RestController
@RequiredArgsConstructor
public class ProblemManageController {

    private final RegisterProblemSetUseCase registerProblemSetUseCase;
    private final UpdateProblemSetUseCase updateProblemSetUseCase;
    private final DeleteProblemSetUseCase deleteProblemSetUseCase;

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

    @PutMapping("/api/v1/problems/{problemSetId}")
    public ResponseEntity<ApiResponse<ProblemSetUpdateResponse>> updateProblemSet(
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

    @DeleteMapping("/api/v1/problems/{problemSetId}")
    public ResponseEntity<ApiResponse<ProblemSetDeleteResponse>> deleteProblemSet(
            @PathVariable Long problemSetId,
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
