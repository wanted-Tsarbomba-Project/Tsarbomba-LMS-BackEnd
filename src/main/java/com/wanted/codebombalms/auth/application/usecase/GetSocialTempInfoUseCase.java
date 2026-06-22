package com.wanted.codebombalms.auth.application.usecase;

import com.wanted.codebombalms.auth.domain.model.OAuthTempData;

public interface GetSocialTempInfoUseCase {

    /** TEMP_TOKEN 으로 신규 회원 임시정보(email/name) 조회 — 추가정보 페이지 표시용 */
    OAuthTempData getTempInfo(String tempToken);
}
