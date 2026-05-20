// 400 Bad Request 클라이언트 요청이 잘못됨 입력값 오류, 빈 필드
package com.wanted.codebombalms.global.domain.common.error.exception;

import com.wanted.codebombalms.global.domain.common.error.BusinessException;
import com.wanted.codebombalms.global.domain.common.error.ErrorCode;

public class ValidationException extends BusinessException {
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
}