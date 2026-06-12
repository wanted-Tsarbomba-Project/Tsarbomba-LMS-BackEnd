package com.wanted.codebombalms.reward.point.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RewardErrorCode implements ErrorCode {

    REWARD_POINT_ALREADY_GRANTED("RWD-PNT-001", "이미 포인트가 지급된 문제입니다."),
    REWARD_POINT_GRANT_FAILED("RWD-PNT-002", "포인트 지급에 실패했습니다."),
    REWARD_POINT_TASK_NOT_FOUND(
        "RWD-PNT-003",
                "포인트 지급 작업을 찾을 수 없습니다."
    );
    private final String code;
    private final String message;
}

