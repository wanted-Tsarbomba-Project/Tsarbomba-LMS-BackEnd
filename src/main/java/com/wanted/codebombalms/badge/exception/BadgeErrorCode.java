package com.wanted.codebombalms.badge.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeErrorCode implements ErrorCode {

    BADGE_NOT_FOUND(
            "BDG-001",
            "뱃지를 찾을 수 없습니다."
    ),
    BADGE_NAME_ALREADY_EXISTS(
            "BDG-002",
            "이미 사용 중인 뱃지 이름입니다."
    ),
    BADGE_INVALID_INPUT(
            "BDG-003",
            "뱃지 입력값이 올바르지 않습니다."
    ),
    BADGE_IMAGE_INVALID_FILE(
            "BDG-IMG-001",
            "올바르지 않은 뱃지 이미지 파일입니다."
    ),
    BADGE_IMAGE_UPLOAD_FAILED(
            "BDG-IMG-002",
            "뱃지 이미지 업로드에 실패했습니다."
    ),
    BADGE_IMAGE_ACCESS_URL_FAILED(
            "BDG-IMG-003",
            "뱃지 이미지 접근 URL 생성에 실패했습니다."
    ),
    BADGE_IMAGE_DELETE_FAILED(
            "BDG-IMG-004",
            "뱃지 이미지 삭제에 실패했습니다."
    ),
    USER_BADGE_NOT_FOUND(
            "BDG-USR-001",
            "사용자가 보유한 뱃지를 찾을 수 없습니다."
    );

    private final String code;
    private final String message;
}
