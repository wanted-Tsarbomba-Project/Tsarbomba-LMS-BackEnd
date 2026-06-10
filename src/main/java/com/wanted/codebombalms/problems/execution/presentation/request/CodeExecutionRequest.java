package com.wanted.codebombalms.problems.execution.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CodeExecutionRequest {

    @Schema(
            description = "실행할 Python 코드. 코드 실행은 채점과 다르며 제출 기록을 생성하지 않습니다.",
            example = """
                    import pandas as pd
                    import os
                    
                    df = pd.read_csv(os.environ["DATASET_PATH"])
                    result = df.shape
                    print(result)
                    """
    )
    @NotBlank(message = "실행할 코드는 필수입니다.")
    @Size(max = 10000, message = "실행할 코드는 10000자 이하여야 합니다.")
    private String code;

    public String getCode() {
        return code;
    }
}
