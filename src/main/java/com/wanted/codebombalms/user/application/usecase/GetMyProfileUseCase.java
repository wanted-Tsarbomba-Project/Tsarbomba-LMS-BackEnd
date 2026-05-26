package com.wanted.codebombalms.user.application.usecase;

import com.wanted.codebombalms.user.application.query.MyProfileResult;

public interface GetMyProfileUseCase {

    MyProfileResult getMyProfile(Long userId);
}