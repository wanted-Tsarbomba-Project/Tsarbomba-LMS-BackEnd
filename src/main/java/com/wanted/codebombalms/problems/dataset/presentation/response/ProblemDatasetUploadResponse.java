package com.wanted.codebombalms.problems.dataset.presentation.response;

import com.wanted.codebombalms.problems.dataset.application.usecase.UploadProblemDatasetUseCase.UploadProblemDatasetView;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemDatasetUploadResponse(
        @Schema(description = "업로드된 데이터셋 ID", example = "3001")
        Long datasetId,

        @Schema(description = "사용자가 업로드한 원본 파일명", example = "employee_performance.csv")
        String originalFileName,

        @Schema(description = "GCS에 저장된 UUID 기반 파일명", example = "28679fde-6075-4c2e-a3f0-190fa3d80db7_employee_performance.csv")
        String storedFileName,

        @Schema(description = "브라우저와 코드 실행기가 접근할 수 있는 GCS URL", example = "https://storage.googleapis.com/codebombalms/problem_dataset/28679fde-6075-4c2e-a3f0-190fa3d80db7_employee_performance.csv")
        String fileUrl,

        @Schema(description = "GCS 버킷 내부 객체 경로", example = "problem_dataset/28679fde-6075-4c2e-a3f0-190fa3d80db7_employee_performance.csv")
        String filePath,

        @Schema(description = "데이터셋 상태", example = "ACTIVE")
        String status
) {
    public ProblemDatasetUploadResponse(UploadProblemDatasetView result) {
        this(
                result.datasetId(),
                result.originalFileName(),
                result.storedFileName(),
                result.fileUrl(),
                result.filePath(),
                result.status()
        );
    }
}