package com.wanted.codebombalms.domain.problems.dataset.controller;


import com.wanted.codebombalms.domain.problems.dataset.dto.request.ProblemDatasetConnectRequest;
import com.wanted.codebombalms.domain.problems.dataset.dto.response.ProblemDatasetConnectResponse;
import com.wanted.codebombalms.domain.problems.dataset.dto.response.ProblemDatasetUploadResponse;
import com.wanted.codebombalms.domain.problems.dataset.service.ProblemDatasetService;
import com.wanted.codebombalms.global.common.ResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ProblemDatasetController {
    private final ProblemDatasetService problemDatasetService;

    @PostMapping("/api/v1/problems/{problemId}/datasets")
    public ResponseEntity<ResponseDTO> connectDataset(
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemDatasetConnectRequest request
    ) {
        ProblemDatasetConnectResponse response =
                problemDatasetService.connectDataset(problemId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO(HttpStatus.CREATED, "문제-데이터셋 연결 성공", response));
    }

    @PostMapping(
            value = "/api/v1/problem-datasets",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ResponseDTO> uploadDataset(
            @RequestPart("datasetFile") MultipartFile datasetFile
    ) {
        ProblemDatasetUploadResponse response =
                problemDatasetService.uploadDataset(datasetFile);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO(HttpStatus.CREATED, "데이터셋 업로드 성공", response));
    }


}
