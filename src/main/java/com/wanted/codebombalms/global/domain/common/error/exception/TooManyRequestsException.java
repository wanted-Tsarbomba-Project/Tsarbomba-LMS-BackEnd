// 429 Too Many Requests
package com.wanted.codebombalms.global.domain.common.error.exception;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.global.domain.common.error.ErrorCode;

public class TooManyRequestsException extends DomainException {

    public TooManyRequestsException(ErrorCode errorCode) {
        super(errorCode);
    }

    @Override
    public int getHttpStatus() {
        return 429;
    }
}