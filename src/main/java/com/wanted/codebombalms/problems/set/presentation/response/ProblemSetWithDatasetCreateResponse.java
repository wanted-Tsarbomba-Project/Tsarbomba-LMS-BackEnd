package com.wanted.codebombalms.problems.set.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemSetWithDatasetCreateResponse(
        @Schema(description = "생성된 문제 세트 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "문제 세트에 연결된 데이터셋 ID", example = "5001")
        Long datasetId,

        @Schema(description = "문제 세트 제목", example = "pandas 기초 분석 문제 세트")
        String title,

        @Schema(description = "카테고리명", example = "Python 데이터 분석")
        String categoryName,

        @Schema(description = "전체 소문제 수", example = "1")
        Integer totalProblemCount,

        @Schema(description = "생성된 소문제 수", example = "1")
        Integer createdProblemCount,

        @Schema(description = "생성된 테스트케이스 수", example = "2")
        Integer createdTestCaseCount,

        @Schema(description = "업로드한 원본 CSV 파일명", example = "employee_performance.csv")
        String datasetFileName,

        @Schema(description = "학생 코드 에디터에 제공할 pandas 시작 코드", example = "import os\nimport pandas as pd\n\ndf = pd.read_csv(os.environ[\"DATASET_PATH\"])")
        String startCode
) {
}
