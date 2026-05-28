package com.wanted.codebombalms.ranking.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RankingErrorCode implements ErrorCode {

    RANKING_NOT_FOUND("RNK-001", "랭킹 정보를 찾을 수 없습니다.");

    private final String code;
    private final String message;
}
