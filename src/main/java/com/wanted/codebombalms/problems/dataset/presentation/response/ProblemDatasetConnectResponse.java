package com.wanted.codebombalms.problems.dataset.presentation.response;

import com.wanted.codebombalms.problems.dataset.application.usecase.ConnectProblemDatasetUseCase.ConnectProblemDatasetView;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemDatasetConnectResponse(
        @Schema(description = "데이터셋이 연결된 문제 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "연결된 데이터셋 ID", example = "3001")
        Long datasetId,

        @Schema(
                description = "코드 에디터에 제공할 시작 코드. 연결된 GCS 데이터셋 URL을 pandas로 읽습니다.",
                example = """
                        import pandas as pd

                        df = pd.read_csv("https://storage.googleapis.com/codebombalms/problem_dataset/28679fde-6075-4c2e-a3f0-190fa3d80db7_employee_performance.csv")
                        """
        )
        String startCode
) {
    public ProblemDatasetConnectResponse(ConnectProblemDatasetView result) {
        this(
                result.problemSetId(),
                result.datasetId(),
                result.startCode()
        );
    }
}
