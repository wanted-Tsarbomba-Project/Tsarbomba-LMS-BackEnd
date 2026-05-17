// 401 Unauthorized 인증 안 됨 (로그인 안 함) 토큰 없음, 토큰 만료
package com.wanted.codebombalms.global.error.exception;

import com.wanted.codebombalms.global.error.BusinessException;
import com.wanted.codebombalms.global.error.ErrorCode;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}