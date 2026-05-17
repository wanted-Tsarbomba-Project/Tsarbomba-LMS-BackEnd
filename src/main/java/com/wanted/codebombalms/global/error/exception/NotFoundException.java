// 404 NotFound 리소스 없음 없는 Id조회
package com.wanted.codebombalms.global.error.exception;

import com.wanted.codebombalms.global.error.BusinessException;
import com.wanted.codebombalms.global.error.ErrorCode;

public class NotFoundException extends BusinessException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}