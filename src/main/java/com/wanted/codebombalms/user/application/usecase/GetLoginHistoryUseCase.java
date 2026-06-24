package com.wanted.codebombalms.user.application.usecase;

import com.wanted.codebombalms.auth.domain.model.LoginHistory;

import java.util.List;

public interface GetLoginHistoryUseCase {

    List<LoginHistory> getLoginHistory(Long userId, int page);
}
