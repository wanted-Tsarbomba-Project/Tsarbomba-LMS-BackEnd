package com.wanted.codebombalms.user.application.usecase;

import com.wanted.codebombalms.user.application.query.UpdateMyInfoResult;

public interface UpdateMyInfoUseCase {

    UpdateMyInfoResult update(Long userId, String nickname, String phone);
}
