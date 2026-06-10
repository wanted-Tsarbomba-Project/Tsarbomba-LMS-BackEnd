package com.wanted.codebombalms.global.domain.common.error.exception;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.global.domain.common.error.ErrorCode;

public class ExternalServiceException extends DomainException {
    public ExternalServiceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ExternalServiceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    @Override
    public int getHttpStatus() {
        return 502;
    }
}
