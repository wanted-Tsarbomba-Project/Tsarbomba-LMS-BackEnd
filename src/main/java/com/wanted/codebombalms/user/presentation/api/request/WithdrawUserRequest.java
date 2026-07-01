package com.wanted.codebombalms.user.presentation.api.request;

public record WithdrawUserRequest(

        // LOCAL 계정: 현재 비밀번호 (본인 재확인)
        String password,

        // 소셜 계정: 확인 문구 ("탈퇴하겠습니다")
        String confirmText
) {
}
