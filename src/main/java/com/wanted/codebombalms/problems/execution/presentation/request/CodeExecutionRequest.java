package com.wanted.codebombalms.problems.execution.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

public class CodeExecutionRequest {

    @Schema(
            description = "실행할 Python 코드. 코드 실행은 채점과 다르며 제출 기록을 생성하지 않습니다.",
            example = """
                    import pandas as pd

                    df = pd.read_csv("https://storage.googleapis.com/codebombalms/problem_dataset/employee_performance.csv")
                    result = df.shape
                    print(result)
                    """
    )
    private String code;

    public String getCode() {
        return code;
    }
}
