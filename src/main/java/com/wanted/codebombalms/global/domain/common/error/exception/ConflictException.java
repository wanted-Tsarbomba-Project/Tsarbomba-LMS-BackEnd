// Conflict 409 (이미 존재) 중복 이메일, 이미 완료된 항목
package com.wanted.codebombalms.global.domain.common.error.exception;

import com.wanted.codebombalms.global.domain.common.error.BusinessException;
import com.wanted.codebombalms.global.domain.common.error.ErrorCode;

public class ConflictException extends BusinessException {
    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}