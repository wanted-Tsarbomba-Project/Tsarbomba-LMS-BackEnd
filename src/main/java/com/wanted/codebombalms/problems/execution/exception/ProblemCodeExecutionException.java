package com.wanted.codebombalms.problems.execution.exception;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;

public class ProblemCodeExecutionException extends DomainException {

    public ProblemCodeExecutionException() {
        super(ProblemErrorCode.PROBLEM_CODE_EXECUTION_FAILED);
    }

    @Override
    public int getHttpStatus() {
        return 500;
    }
}
