// 401 Unauthorized 인증 안 됨 (로그인 안 함) 토큰 없음, 토큰 만료
package com.wanted.codebombalms.global.domain.common.error.exception;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.global.domain.common.error.ErrorCode;

public class UnauthorizedException extends DomainException {
    public UnauthorizedException(ErrorCode errorCode) { super(errorCode); }

    @Override
    public int getHttpStatus() { return 401; }
}