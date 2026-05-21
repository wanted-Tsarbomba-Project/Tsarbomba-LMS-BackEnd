package com.wanted.codebombalms.problems.dataset.presentation.api;

import com.wanted.codebombalms.problems.dataset.application.command.ConnectProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.usecase.ConnectProblemDatasetUseCase;
import com.wanted.codebombalms.problems.dataset.application.usecase.UploadProblemDatasetUseCase;
import com.wanted.codebombalms.problems.dataset.presentation.api.request.ProblemDatasetConnectRequest;
import com.wanted.codebombalms.problems.dataset.presentation.api.response.ProblemDatasetConnectResponse;
import com.wanted.codebombalms.problems.dataset.presentation.api.response.ProblemDatasetUploadResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
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

@RestController
@RequiredArgsConstructor
public class ProblemDatasetController {

    private final ConnectProblemDatasetUseCase connectProblemDatasetUseCase;
    private final UploadProblemDatasetUseCase uploadProblemDatasetUseCase;

    @PostMapping("/api/v1/problems/{problemId}/datasets")
    public ResponseEntity<ApiResponse<ProblemDatasetConnectResponse>> connectDataset(
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemDatasetConnectRequest request
    ) {
        var command = new ConnectProblemDatasetCommand(problemId, request.datasetId());
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

    @PostMapping(
            value = "/api/v1/problem-datasets",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ProblemDatasetUploadResponse>> uploadDataset(
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
