// 404 NotFound 리소스 없음 없는 Id조회
package com.wanted.codebombalms.global.domain.common.error.exception;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.global.domain.common.error.ErrorCode;

public class NotFoundException extends DomainException {
    public NotFoundException(ErrorCode errorCode) { super(errorCode); }

    @Override
    public int getHttpStatus() { return 404; }
}