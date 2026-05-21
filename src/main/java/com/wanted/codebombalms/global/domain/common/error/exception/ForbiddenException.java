// 403 Forbidden 인가 안 됨(권한 없음) 로그인은 됐는데 접근 불가
package com.wanted.codebombalms.global.domain.common.error.exception;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.global.domain.common.error.ErrorCode;

public class ForbiddenException extends DomainException {
    public ForbiddenException(ErrorCode errorCode) { super(errorCode); }

    @Override
    public int getHttpStatus() { return 403; }
}