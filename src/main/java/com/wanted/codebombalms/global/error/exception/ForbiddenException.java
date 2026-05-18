// 403 Forbidden 인가 안 됨(권한 없음) 로그인은 됐는데 접근 불가
package com.wanted.codebombalms.global.error.exception;

import com.wanted.codebombalms.global.error.BusinessException;
import com.wanted.codebombalms.global.error.ErrorCode;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }
}